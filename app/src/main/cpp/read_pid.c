#include "read_pid.h"

#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <ctype.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

static inline char *find_name(char *stat_line, char *buf, size_t bufsize) {
  char *begin = strchr(stat_line, '(');
  if (begin == NULL) {
    return NULL;
  }
  char *end = strrchr(stat_line, ')');
  if (end == NULL) {
    return NULL;
  }

  if ((size_t) (end - begin) > bufsize) {
    return NULL;
  }

  size_t count = 0;
  for (char *p = begin + 1; p < end; p++) {
    char c = *p;
    if (c < 0 || !isprint(c)) {
      c = '?';
    }
    buf[count++] = c;
    if (count >= bufsize) {
      return NULL;
    }
  }
  buf[count] = '\0';
  return end + 1;
}

bool read_pid(int pid, struct procinfo *info, char *name, size_t name_len) {
  info->pid = pid;
  int fd;
  char buf[1024];
  snprintf(buf, sizeof(buf), "/proc/%d", pid);
  struct stat statbuf;
  if (stat(buf, &statbuf)) {
    return false;
  }
  info->uid = statbuf.st_uid;
  snprintf(buf, sizeof(buf), "/proc/%d/stat", pid);
  fd = open(buf, O_RDONLY);
  if (fd == -1) {
    return false;
  }

  ssize_t nread = read(fd, buf, sizeof(buf) - 1);
  if (nread < 0) {
    close(fd);
    return false;
  }
  buf[nread] = '\0';
  close(fd);

  char *p = find_name(buf, name, name_len);
  if (!p) {
    return false;
  }

  unsigned long utime, stime;
  long rss;
  if (sscanf(p,
             " %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lu %lu %*d %*d %*d %*d %*d %*d %*u %*u %ld",
             &utime,
             &stime,
             &rss) != 3) {
    close(fd);
    return false;
  }

  info->time = utime + stime;
  info->resident = rss;

  close(fd);
  return true;
}

size_t read_cmdline(int pid, char *buf, size_t bufsize) {
  int fd;
  snprintf(buf, bufsize, "/proc/%d/cmdline", pid);
  fd = open(buf, O_RDONLY);
  if (fd == -1) {
    return 0;
  }

  ssize_t nread = read(fd, buf, bufsize - 1);
  if (nread < 0) {
    close(fd);
    return 0;
  }
  buf[nread] = '\0';
  close(fd);

  size_t n = strnlen(buf, nread);
  for (size_t i = 0; i < n; i++) {
    char c = buf[i];
    if (c < 0 || !isprint(c)) {
      buf[i] = '?';
    }
  }
  return n;
}
