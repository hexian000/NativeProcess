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
  stat_line = strchr(stat_line, '(');
  if (stat_line == NULL) {
	return NULL;
  }
  size_t count = 0;
  int b = 1;
  for (char c = *++stat_line; c && count < bufsize; c = *++stat_line) {
	switch (c) {
	  case '(': b++;
		break;
	  case ')':b--;
		if (b == 0) {
		  buf[count] = 0;
		  return stat_line + 1;
		}
		break;
	  default:
		if (c < 0 || !isprint(c)) {
		  c = '?';
		}
		buf[count++] = c;
	}
  }
  return NULL;
}

bool read_pid(int pid, struct procinfo *info, char *name, size_t name_len) {
  info->pid = pid;
  int fd;
  char buf[256];
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

  ssize_t n = read(fd, buf, sizeof(buf));
  if (n < 0) {
	return false;
  }
  buf[n] = '\0';

  char *p = find_name(buf, name, name_len);
  if (p == NULL) {
	return false;
  }

  unsigned long utime, stime;
  long rss;
  if (sscanf(p, " %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lu %lu %*d %*d %*d %*d %*d %*d %*u %*u %ld",
			 &utime, &stime, &rss) != 3) {
	return false;
  }

  info->time = utime + stime;
  info->resident = rss;

  return true;
}
