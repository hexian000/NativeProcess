#ifndef TASKS_READ_PID_H
#define TASKS_READ_PID_H

#include "procinfo.h"

#include <stdbool.h>
#include <stddef.h>

bool read_pid(int pid, struct procinfo *info, char *name, size_t name_len);

#endif //TASKS_READ_PID_H
