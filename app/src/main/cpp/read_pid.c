#include "read_pid.h"

#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <ctype.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

bool read_pid(int pid, struct procinfo *info, char *name, size_t name_len) {
  info->pid = pid;
  int fd;
  char buf[1024];
  snprintf(buf, sizeof(buf), "/proc/%d/stat", pid);
  struct stat statbuf;
  if (stat(buf, &statbuf)) {
    return false;
  }
  info->uid = statbuf.st_uid;
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

  unsigned long utime, stime;
  long rss;
  if (sscanf(buf,
             "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lu %lu %*d %*d %*d %*d %*d %*d %*u %*u %ld",
             &utime,
             &stime,
             &rss) != 3) {
    close(fd);
    return false;
  }

  info->time = utime + stime;
  info->resident = rss;

  close(fd);
  snprintf(buf, sizeof(buf), "/proc/%d/cmdline", pid);
  fd = open(buf, O_RDONLY);
  if (fd == -1) {
    return false;
  }

  nread = read(fd, name, name_len - 1);
  if (nread < 0) {
    close(fd);
    return false;
  }
  name[nread] = '\0';

  for (size_t i = 0; i < (size_t) nread; i++) {
    if (!isprint(name[i])) {
      name[i] = '?';
    }
  }

  close(fd);
  return true;
}
