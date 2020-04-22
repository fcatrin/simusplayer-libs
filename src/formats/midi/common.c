#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include "common.h"

void log_error(const char *msg, ...) {
	va_list ap;

	va_start(ap, msg);
	vfprintf(stderr, msg, ap);
	va_end(ap);
	fputc('\n', stderr);
	fflush(stderr);
}

void log_fatal(const char *msg, ...) {
	va_list ap;

	va_start(ap, msg);
	vfprintf(stderr, msg, ap);
	va_end(ap);
	fputc('\n', stderr);
	fflush(stderr);
	exit(EXIT_FAILURE);
}

void check_mem(void *p) {
	if (!p) log_fatal("Out of memory");
}
