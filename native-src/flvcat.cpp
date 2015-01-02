#include <iostream>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include "flvcat.h"
using namespace std;
#define  _CRT_SECURE_NO_WARNINGS
unsigned int BigEndiantoLittleEndian32(unsigned char *num)
{
	unsigned int nResult = 0;
	nResult = num[0] << 24 | num[1] << 16 | num[2] << 8 | num[3];
	return nResult;
}

// 获得flv视频播放时间
unsigned int *GetFlvTime(FILE * pFile)
{
	bool isVideo = false;
	bool isAudio = false;
	unsigned int *time = (unsigned int *)malloc(sizeof(unsigned int) * 2);
	time[0] = 0;
	time[1] = 0;
	unsigned char presize[4];
	FLVTag tag = { 0 };
	// FILE *pFile=fopen(name,"r"); //打开flv文件
	if (pFile == NULL)
	{
		printf("Files not found!\n");
		return NULL;
	}
	else
	{
		fseek(pFile, 0, SEEK_END);
		long pos;
		unsigned int size;
		pos = ftell(pFile);
		fseek(pFile, pos - 4, SEEK_SET);
		while (true)
		{
			pos = ftell(pFile);
			fread(presize, 4, 1, pFile);
			size = BigEndiantoLittleEndian32(presize);
			fseek(pFile, pos - size, SEEK_SET);
			pos = ftell(pFile);
			fread(&tag, sizeof(FLVTag), 1, pFile);
			unsigned char tagtype = tag.reserved2_filer1_tagtype5 & 0x1f;
			if (tagtype == 9)
			{
				time[0] = tag.timestamp[0] << 16 | tag.timestamp[1] << 8 | tag.timestamp[2];
				isVideo = true;
				if (isAudio)
					break;
			}
			if (tagtype == 8)
			{
				time[1] = tag.timestamp[0] << 16 | tag.timestamp[1] << 8 | tag.timestamp[2];
				isAudio = true;
				if (isVideo)
					break;
			}
			fseek(pFile, pos - 4, SEEK_SET);
		}
		// fclose(pFile);
	}
	return time;
}

// 改变每个video后audio tag的时间戳
void ChangeTimeStampAndWrite(unsigned int &videosum, unsigned int &audiosum, FILE * pDest,
							 FILE * pSource)
{
	fseek(pSource, 0, SEEK_END);
	unsigned int endpos = ftell(pSource);
	rewind(pSource);
	fseek(pSource, 13, SEEK_SET);	// 跳过flv文件头
	unsigned int pos = ftell(pSource);
	FLVTag tag = { 0 };
	unsigned char tagtype;
	unsigned int datasize = 0;
	int video_sum = 0;
	int audio_sum = 0;
	while (true)
	{
		fread(&tag, sizeof(FLVTag), 1, pSource);
		tagtype = tag.reserved2_filer1_tagtype5 & 0x1f;
		if (tagtype == 18)		// script data
		{
			// do nothing
		}
		else if (tagtype == 9)	// video
		{
			video_sum =
				videosum + (tag.timestamp[0] << 16 | tag.timestamp[1] << 8 | tag.timestamp[2]);
			tag.timestamp[0] = (unsigned char)((video_sum & 0x00ff0000) >> 16);
			tag.timestamp[1] = (unsigned char)((video_sum & 0x0000ff00) >> 8);
			tag.timestamp[2] = (unsigned char)((video_sum & 0x000000ff));
			fwrite(&tag, sizeof(FLVTag), 1, pDest);
		}
		else					// audio
		{
			audio_sum =
				audiosum + (tag.timestamp[0] << 16 | tag.timestamp[1] << 8 | tag.timestamp[2]);
			tag.timestamp[0] = (unsigned char)((audio_sum & 0x00ff0000) >> 16);
			tag.timestamp[1] = (unsigned char)((audio_sum & 0x0000ff00) >> 8);
			tag.timestamp[2] = (unsigned char)((audio_sum & 0x000000ff));
			fwrite(&tag, sizeof(FLVTag), 1, pDest);
		}
		datasize = tag.datasize[0] << 16 | tag.datasize[1] << 8 | tag.datasize[2];
		pos += datasize + 11 + 4;
		if (tagtype != 18)
		{
			unsigned int size = datasize + 15 - sizeof(FLVTag);
			char *other = (char *)malloc(size);
			fread(other, size, 1, pSource);
			fwrite(other, size, 1, pDest);
			free(other);
		}

		if (pos >= endpos)
		{
			break;
		}
		fseek(pSource, pos, SEEK_SET);
	}
}

// 无符号16位整数的大端格式转小端
unsigned short BigToLittleUI16(unsigned short n)
{
	unsigned short result;
	result = ((n & 0xff00) >> 8) | ((n & 0x00ff) << 8);
	return result;
}

// 无符号32位整数的大端格式转小端
unsigned int BigToLittleUI32(unsigned int n)
{
	unsigned int result;
	result =
		((n & 0xff000000) >> 24) | ((n & 0x00ff0000) >> 8) | ((n & 0x0000ff00) << 8) |
		((n & 0x000000ff) << 24);
	return result;
}

// 修改duration的值
void ResetDuration(FILE * file, long double newduration)
{

	unsigned char *p = (unsigned char *)&newduration;
	// 小端格式转大端格式
	for (int i = 0; i < 4; i++)
	{
		unsigned char temp;
		temp = p[i];
		p[i] = p[7 - i];
		p[7 - i] = temp;
	}
	fseek(file, 13, SEEK_SET);
	FLVTag flvtag;
	// 读取onMetadata信息
	fread(&flvtag, sizeof(FLVTag), 1, file);
	if ((flvtag.reserved2_filer1_tagtype5 & 0x1f) != 18)
	{
		return;
	}
	fseek(file, 24, SEEK_SET);
	unsigned char type;
	int len = 0;
	fread(&type, 1, 1, file);
	unsigned short strsize;
	fread(&strsize, 2, 1, file);
	strsize = BigToLittleUI16(strsize);
	long pos = ftell(file);
	fseek(file, pos + strsize, SEEK_SET);
	fread(&type, 1, 1, file);
	unsigned int size;
	fread(&size, 4, 1, file);
	size = BigToLittleUI32(size);
	char *str;
	// 遍历onMetaData数组
	for (unsigned int i = 1; i <= size; i++)
	{
		fread(&strsize, 2, 1, file);
		strsize = BigToLittleUI16(strsize);
		str = (char *)malloc(strsize + 1);
		str[strsize] = 0;
		fread(str, strsize, 1, file);
		// 找到duration项修改播放时间
		if (strcmp(str, "duration") == 0)
		{
			unsigned char ui8;
			fread(&ui8, 1, 1, file);
			long double temp;
			pos = ftell(file);
			fread(&temp, 8, 1, file);
			fseek(file, pos, SEEK_SET);
			// 修改duration的值
			fwrite(&newduration, 8, 1, file);
			break;
		}
		else
		{
			fread(&type, 1, 1, file);
			pos = ftell(file);
			if (type == 0)		// double
			{
				fseek(file, pos + 8, SEEK_SET);
			}
			else if (type == 1)	// boolean
			{
				fseek(file, pos + 1, SEEK_SET);
			}
			else if (type == 2)	// scriptdatastring
			{
				fread(&strsize, 2, 1, file);
				pos = ftell(file);
				strsize = BigToLittleUI16(strsize);
				fseek(file, pos + strsize, SEEK_SET);
			}
			else
			{
				break;
			}
		}
		// 释放资源
		free(str);
	}
}

int main(int argc, char **argv)
{
	FILE *pDest;
	FILE *pFirst;
	FILE *pOthers;
	if (argc < 3)
	{
		printf("Arguments error!\n");
	}
	else
	{
		printf("Processing %s ……", argv[2]);
		// 若目标文件存在则清空
		pDest = fopen(argv[1], "w+");
		fclose(pDest);
		pDest = fopen(argv[1], "ab+");
		pFirst = fopen(argv[2], "rb+");
		unsigned int videosum = 0, audiosum = 0;
		unsigned int *time = GetFlvTime(pFirst);
		videosum = time[0];
		audiosum = time[1];
		fseek(pFirst, 0, SEEK_END);
		unsigned int size = ftell(pFirst);
		rewind(pFirst);
		unsigned char *data = (unsigned char *)malloc(size);
		memset(data, 0, size);
		fread(data, size, 1, pFirst);
		fwrite(data, size, 1, pDest);
		free(data);
		fclose(pFirst);
		printf("done!\n");
		// 按参数遍历所有视频源文件(除第一个源文件)
		for (int i = 3; i < argc; i++)
		{
			printf("Processing %s ……", argv[i]);
			pOthers = fopen(argv[i], "rb+");
			ChangeTimeStampAndWrite(videosum, audiosum, pDest, pOthers);
			time = GetFlvTime(pOthers);
			videosum += time[0];
			audiosum += time[1];
			fclose(pOthers);
			printf("done!\n");
		}
		free(time);
		fclose(pDest);
		pDest = fopen(argv[1], "rb+");
		// 获得总时间
		long double duration = (long double)(videosum) / 1000;
		// 重设duration的值
		ResetDuration(pDest, duration);
		fclose(pDest);
		printf("Everything ok!\n");
	}
	return 0;
}