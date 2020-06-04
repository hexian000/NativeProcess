#ifndef TASKS_PROCINFO_H
#define TASKS_PROCINFO_H

struct procinfo {
    unsigned uid;
    int pid;
    unsigned long time;
    long resident;
};

#endif //TASKS_PROCINFO_H
