#ifndef TASKS_READ_PID_H
#define TASKS_READ_PID_H

#include "procinfo.h"

#include <stdbool.h>
#include <stddef.h>

bool read_pid(int pid, struct procinfo *info, char *name, size_t name_len);

size_t read_cmdline(int pid, char *buf, size_t bufsize);

#endif //TASKS_READ_PID_H
