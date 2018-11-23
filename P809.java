package com.ctfo.tools.protocol809Test;

import java.nio.ByteBuffer;

public class P809 {
	private ByteBuffer buf = null;// 报文数据包
	
	private byte phead = 0x00;// 头标识
	
	private byte[] dhead = new byte[22];// 数据头
	
	private byte[] dbody = null;// 数据体
	
	private byte[] crc16 = new byte[2];// CRC校验
	
	private byte ptail = 0x00;// 尾标识
	
	/**********************头信息***********************/
	byte[] length = new byte[4];// 数据包长度
	byte[] sequeen = new byte[4];// 报文序列
	byte[] type = new byte[2];// 业务类型
	byte[] acccode = new byte[4];// 接入码
	byte[] version = new byte[3];// 协议版本
	byte pwdable = 0x00;// 是否加密
	byte[] pwdparam = new byte[4];// 加密参数

	public P809(ByteBuffer buf) {
		this.buf = buf;
		format();// 格式化
	}
	
	public void format() {
		int len = buf.limit();
		phead = buf.get(0);
		if (phead != 0x5b) {
			System.out.println("ERROR:头标识错误");
			return;
		}
		buf.position(1);
		buf.get(dhead);
		
		dbody = new byte[len-26];
		buf.position(23);
		buf.get(dbody);
		
		buf.position(len-3);
		buf.get(crc16);
		byte[] vdata = new byte[buf.limit()-4];
		buf.position(1);
		buf.get(vdata);
		if (bytesToShort(crc16) != bytesToShort(crc16(vdata))) {
			System.out.println("ERROR:CRC16校验不正确");
			//return;
		}
		ptail = buf.get(len-1);
		if (ptail != 0x5d) {
			System.out.println("ERROR:尾标识错误");
			return;
		}
		parseHead();
	}
	
	private void parseHead() {
		ByteBuffer headbuf = ByteBuffer.wrap(dhead);
		headbuf.position(0);
		headbuf.get(length);
		if (bytesToInt(length) != buf.limit()) {
			System.out.println("ERROR:数据长度不正确");
			//return;
		}
		headbuf.position(4);
		headbuf.get(sequeen);
		
		headbuf.position(8);
		headbuf.get(type);
		
		headbuf.position(10);
		headbuf.get(acccode);
		
		headbuf.position(14);
		headbuf.get(version);
		
		pwdable = headbuf.get(17);
		
		headbuf.position(18);
		headbuf.get(pwdparam);
	}
	
	private byte[] crc16(byte[] bufData) {
		int CRC = 0x0000ffff;
		int POLYNOMIAL = 0x0000a001;
		int i, j;
		for (i = 0; i < bufData.length; i++) {
			CRC ^= ((int) bufData[i] & 0x000000ff);
			for (j = 0; j < 8; j++) {
				if ((CRC & 0x00000001) != 0) {
					CRC >>= 1;
					CRC ^= POLYNOMIAL;
				} else {
					CRC >>= 1;
				}
			}
		}
		byte[] res = new byte[]{(byte)(CRC & 0x00ff), (byte)(CRC >> 8)};
		return res;
	}

	/** 字节数组转换为整型 */
	public int bytesToInt(byte b[]) {
		return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
	}
	
	/** 字节数组转换为短整型 */
	public short bytesToShort(byte[] b) {
		return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
	}
	
	public ByteBuffer getBuf() {
		return buf;
	}

	public void setBuf(ByteBuffer buf) {
		this.buf = buf;
	}

	public byte getPhead() {
		return phead;
	}

	public void setPhead(byte phead) {
		this.phead = phead;
	}

	public byte[] getDhead() {
		return dhead;
	}

	public void setDhead(byte[] dhead) {
		this.dhead = dhead;
	}

	public byte[] getDbody() {
		return dbody;
	}

	public void setDbody(byte[] dbody) {
		this.dbody = dbody;
	}

	public byte[] getCrc16() {
		return crc16;
	}

	public void setCrc16(byte[] crc16) {
		this.crc16 = crc16;
	}

	public byte getPtail() {
		return ptail;
	}

	public void setPtail(byte ptail) {
		this.ptail = ptail;
	}

	public byte[] getLength() {
		return length;
	}

	public void setLength(byte[] length) {
		this.length = length;
	}

	public byte[] getSequeen() {
		return sequeen;
	}

	public void setSequeen(byte[] sequeen) {
		this.sequeen = sequeen;
	}

	public byte[] getType() {
		return type;
	}

	public void setType(byte[] type) {
		this.type = type;
	}

	public byte[] getAcccode() {
		return acccode;
	}

	public void setAcccode(byte[] acccode) {
		this.acccode = acccode;
	}

	public byte[] getVersion() {
		return version;
	}

	public void setVersion(byte[] version) {
		this.version = version;
	}

	public byte getPwdable() {
		return pwdable;
	}

	public void setPwdable(byte pwdable) {
		this.pwdable = pwdable;
	}

	public byte[] getPwdparam() {
		return pwdparam;
	}

	public void setPwdparam(byte[] pwdparam) {
		this.pwdparam = pwdparam;
	}
	
	public static void main(String[] args) {
		long IMAX = Integer.MAX_VALUE * 2L + 2;
		System.out.println("FFFFFFFF="+Long.parseLong("FFFFFFFF", 16));
		System.out.println("Integer.MAX_VALUE = "+(Integer.MAX_VALUE));
		System.out.println("Integer.MAX_VALUE+1 = " + (Integer.MAX_VALUE+1));
		System.out.println("Integer.MAX_VALUE+3 = " + (Integer.MAX_VALUE+3));
		System.out.println("Integer.MAX_VALUE * 2L + 1 = " + (Integer.MAX_VALUE * 2L + 1));
		System.out.println("(int)4294967298L = "+(int)4294967298L);
		System.out.println(Integer.toBinaryString(-2113027200));
		System.out.println(Long.toBinaryString(2181940096l));
		//System.out.println(Integer.parseInt("10000010000011011100001110000000", 2));
		System.out.println(Long.parseLong("10000010000011011100001110000000", 2));
		System.out.println(IMAX + (-2113027200 % IMAX));
		System.out.println("2181940096L % 10000000 = "+ 2181940096L % 10000000);
		System.out.println("-2110995584 % 10000000 = "+ -2110995584 % 10000000);
	}
}
