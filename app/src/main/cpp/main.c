#include "read_pid.h"

#include <stdio.h>
#include <stdlib.h>

#include <dirent.h>
#include <unistd.h>
#include <signal.h>
#include <time.h>
#include <pwd.h>

volatile sig_atomic_t last_signal = 0;

void signal_handler(int signum) {
  last_signal = signum;
}

long page_size;

void print_list(DIR *procdir) {
  struct dirent *ent;
  while ((ent = readdir(procdir)) != NULL) {
    char buf[1024];
    pid_t pid = (pid_t) strtol(ent->d_name, NULL, 10);
    if (pid) {
      struct procinfo info;
      if (read_pid(pid, &info, buf, sizeof(buf))) {
        printf("%d,%u,%s,%lu,%ld,%s\n",
               info.pid, info.uid, getpwuid(info.uid)->pw_name,
               info.time,
               info.resident * page_size,
               buf);
      }
      size_t n = read_cmdline(pid, buf, sizeof(buf));
      printf("%*s\n", (int) n, buf);
    }
  }
  printf("END\n");
}

void init() {
  const time_t interval = 5;
  timer_t timer_id;
  signal(SIGALRM, &signal_handler);
  timer_create(CLOCK_MONOTONIC, NULL, &timer_id);
  struct itimerspec ts = {
      .it_value =
      (struct timespec) {
          .tv_sec = interval,
      },
      .it_interval =
      (struct timespec) {
          .tv_sec = interval,
      },
  };
  timer_settime(timer_id, 0, &ts, NULL);
}

int main() {
  const long clock_tick = sysconf(_SC_CLK_TCK);
  printf("%ld\n", clock_tick);
  page_size = sysconf(_SC_PAGESIZE);

  DIR *procdir = opendir("/proc");
  if (!procdir) {
    return EXIT_FAILURE;
  }

  init();

  print_list(procdir);
  while (pause(), last_signal == SIGALRM) {
    rewinddir(procdir);
    print_list(procdir);
  }
  closedir(procdir);

  return EXIT_SUCCESS;
}
