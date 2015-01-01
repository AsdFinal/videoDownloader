#ifndef _TYPE_H_
#define _TYPE_H_
// flv文件头
struct FLVHeader
{
	// 标志
	unsigned char signature_1;
	unsigned char signature_2;
	unsigned char signature_3;
	unsigned char version;		// 版本
	unsigned char typeflag;		// 类型标识
	unsigned int dataoffset;	// 头的长度
};
// flv文件tag
struct FLVTag
{
	unsigned char reserved2_filer1_tagtype5;
	unsigned char datasize[3];	// 信息长度
	unsigned char timestamp[3];	// 时间戳
	unsigned char timestampextended;	// 时间戳的扩展
	unsigned char streamid[3];	// 通常是0
	// ...
};
#endif	/*  */