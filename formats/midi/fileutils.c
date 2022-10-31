#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <common.h>
#include "fileutils.h"

struct stream *stream_open(const char *filename) {
	FILE *f = fopen(filename, "rb");
	if (!f) return NULL;

	struct stream *stream = malloc(sizeof(struct stream));
	stream->file = f;
	stream->offset = 0;
	stream->filename = strdup(filename);
	return stream;
}

void stream_close(struct stream *stream) {
	fclose(stream->file);
	free(stream->filename);
	free(stream);
}

int read_byte(struct stream *stream) {
	stream->offset++;
	return getc(stream->file);
}

void unread_byte(struct stream *stream, int c) {
	stream->offset--;
	ungetc(c, stream->file);
}

int32 read_32_le(struct stream *stream) {
	int32 value;
	value = read_byte(stream);
	value |= read_byte(stream) << 8;
	value |= read_byte(stream) << 16;
	value |= read_byte(stream) << 24;
	return !feof(stream->file) ? value : EOF;
}

int32 read_int(struct stream *stream, uint32 bytes) {
	int32 c, value = 0;

	do {
		c = read_byte(stream);
		if (c == EOF)
			return EOF;
		value = (value << 8) | c;
	} while (--bytes);
	return value;
}

int32 read_var(struct stream *stream) {
	int32 value, c;

	uint8 i = 0;
	value = 0;
	do {
		c = read_byte(stream);
		value = (value << 7) | (c & 0x7f);
	} while ( (c & 0x80) && i++ < 4 && !feof(stream->file));

	return !feof(stream->file) ? value : EOF;
}

void skip(struct stream *stream, uint32 bytes) {
	while (bytes > 0) {
		read_byte(stream);
		--bytes;
	}
}

char *read_string(struct stream *stream, int bytes) {
	char *buffer = malloc(bytes+1);
	for(int i=0; i<bytes; i++) {
		buffer[i] = (char)read_byte(stream);
	}
	buffer[bytes] = 0;
	return buffer;
}

