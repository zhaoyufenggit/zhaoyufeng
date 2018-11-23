package com.ctfo.tools.protocol809Test;

import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class P809Parser {
	private static int M1 = 10000000, IA1 = 20000000, IC1 = 30000000;
	private static int acccode =64000098, userid = 64000098; 
	private static String passwd = "64000098";

	/** 解析协议入口 
	 * @throws UnsupportedEncodingException */
	public void parse(P809 p809, DataOutputStream dos) throws UnsupportedEncodingException {
		byte[] bodyarr = p809.getDbody();// 数据体数组
		if (p809.getPwdable() == 0x01) {// 解密数据体
			int encryptKey = bytes2Int(p809.getPwdparam());
			encrypt(bodyarr, encryptKey);
		}
		
		byte[] type = p809.getType();
		if (type[0] == 0x10) {// 链路管理
			if (type[1] == 0x01) {// 0x1001主链路登录请求消息
				parseLogin(p809, dos);
			} else if (type[1] == 0x02) {// 0x1002主链路登录应答消息
				
			}  else if (type[1] == 0x03) {// 0x1003主链路注销请求消息
				
			}  else if (type[1] == 0x04) {// 0x1004主链路注销应答消息
				
			}  else if (type[1] == 0x05) {// 0x1005主链路连接保持请求消息
				
			}  else if (type[1] == 0x06) {// 0x1006主链路连接保持应答消息
				
			}  else if (type[1] == 0x07) {// 0x1007主链路断开通知消息
				
			}  else if (type[1] == 0x08) {// 0x1008下级平台主动关闭链路通知消息
				
			} else {// 非法数据
				System.out.println("业务类型无法识别：");
			}
		} else if (type[0] == 0x12) {// 0x1200主链路动态信息交换（上报位置）
			if (type[1] == 0x00) {
				this.parsePosit(p809);
			}
		} else if (type[0] == 0x13) {// 0x1300主链路平台间信息交互消息
			
		} else if (type[0] == 0x14) {// 0x1400主链路报警信息交互消息
			
		} else if (type[0] == 0x15) {// 0x1500主链路车辆监管消息
			
		} else if (type[0] == 0x16) {// 0x1600主链路静态信息交换消息
			
		} else {// 非法数据
			System.out.println("业务类型无法识别：");
		}
	
	}
	
	/** 解析登录指令 */
	public void parseLogin(P809 p809, DataOutputStream dos) {
		ByteBuffer bodybuf = ByteBuffer.wrap(p809.getDbody());// 数据体缓冲
		byte[] username = new byte[4];// 用户名
		byte[] password = new byte[8];// 密码
		byte[] ip = new byte[32];// 密码
		byte[] port = new byte[2];// 密码
		bodybuf.position(0);
		bodybuf.get(username);
		
		bodybuf.position(4);
		bodybuf.get(password);
		
		bodybuf.position(12);
		bodybuf.get(ip);
		
		bodybuf.position(44);
		bodybuf.get(port);
		StringBuilder sb = new StringBuilder();
		sb.append("------------登录请求------------\n");
		sb.append("头信息：").append(formatHeadInfo(p809)).append("\n数据体：");
		sb.append(bytes2HexStr(p809.getDbody(), " ")).append("\n");
		sb.append("用户名=").append(bytes2Int(username)).append("->0x");
		sb.append(bytes2HexStr(username, "")).append(", ");
		
		sb.append("密码=").append(new String(password)).append("->0x");
		sb.append(bytes2HexStr(password, "")).append(", ");
		
		sb.append("从链路IP=").append(new String(ip)).append("->0x");
		sb.append(bytes2HexStr(ip, "")).append(", ");
		
		sb.append("从链路端口=").append(bytes2Short(port)).append("->0x");
		sb.append(bytes2HexStr(port, ""));
		System.out.println(sb.toString());
		//////////////////////////////////////////////
		byte repinfo = 0x00;// 应答状态 成功
		if (bytes2Int(p809.getAcccode()) != acccode) repinfo = 0x02;// 接入码错误
		else if (bytes2Int(username) != userid) repinfo = 0x03;// 用户不存在
		else if (!passwd.equals(new String(password))) repinfo = 0x04;// 密码错误
		
		ByteBuffer repbuf = ByteBuffer.allocate(31);
		repbuf.position(0);
		repbuf.put((byte)0x5b);// 头标识
		repbuf.put(int2Bytes(31));// 长度
		repbuf.put(p809.getSequeen());// 序列号
		repbuf.put(new byte[]{0x10, 0x02});// 业务类型
		repbuf.put(p809.getAcccode());// 接入码
		repbuf.put(p809.getVersion());// 版本
		repbuf.put(p809.getPwdable());// 是否加密
		repbuf.put(p809.getPwdparam());// 加密参数
		if (p809.getPwdable() == 0x01) {// 加密数据体
			int encryptKey = bytes2Int(p809.getPwdparam());
			byte[] bs = new byte[]{repinfo};
			encrypt(bs, encryptKey);
			repinfo = bs[0];
		}
		repbuf.put(repinfo);// 状态
		repbuf.put(new byte[4]);// 校验码
		repbuf.put(new byte[2]);// CRC16校验码
		repbuf.put((byte)0x5d);// 尾标识
		repbuf.flip();
		byte[] rep = repbuf.array();
		System.out.println("应答内容："+bytes2HexStr(rep, " "));
		try {
			dos.write(rep);
		} catch (Exception e) {
			System.out.println("ERROR:应答异常:"+getStackTrace(e));
		}
	}
	
	/** 解析位置信息 
	 * @throws UnsupportedEncodingException */
	public void parsePosit(P809 p809) throws UnsupportedEncodingException {
		// 把数据体byte[]数组封装成ByteBuffer对象，方便操作
		ByteBuffer bodyBuffer = ByteBuffer.wrap(p809.getDbody());
		
		// 定义车辆定位信息的成员
		byte[] vehicleNo =  new byte[21]; //车牌号，String类型
		byte[] vehicleColor = new byte[1]; // 车牌颜色，Byte类型。
		byte[] dataType = new byte[2]; // 子业务类型标识，short int类型
		byte[] dataLength = new byte[4]; // 后续数据长度，int类型。
		byte[] gnssData = new byte[36]; // 定位信息数据体
		// 下面解析gnssData内部的子成员
		
		// 移动到0字节位，读取车牌号
		bodyBuffer.position(0);
		bodyBuffer.get(vehicleNo);
		
		// 游标移动到21字节位，读取车牌颜色
		bodyBuffer.position(21);
		bodyBuffer.get(vehicleColor);
		
		// 游标移动到22字节位，读取子业务类型标识
		bodyBuffer.position(22);
		bodyBuffer.get(dataType);
		if (dataType[1] != 0x02) {
			System.out.println("非位置信息：0x"+bytes2HexStr(dataType, ""));
			return;
		}
		
		// 游标移到24字节位，读取后续数据长度
		bodyBuffer.position(24);
		bodyBuffer.get(dataLength);
		
		//  游标移到28字节位，读取定位信息数据体
		bodyBuffer.position(28);
		bodyBuffer.get(gnssData);
		
		// 打印输出
		StringBuffer sb = new StringBuffer("{");
		sb.append("------------车辆位置信息------------\n");
		sb.append("头信息：").append(formatHeadInfo(p809)).append("\n数据体：");
		sb.append("车牌号 =").append(new String(vehicleNo, "GBK")).append("->0x");
		sb.append(bytes2HexStr(vehicleNo, "")).append(", ");
		sb.append("车牌颜色=").append(vehicleColor[0]).append("->0x");
		sb.append(bytes2HexStr(vehicleColor, "")).append(", ");
		sb.append("子类型=").append(bytes2HexStr(dataType, "")).append("->0x");
		sb.append(bytes2HexStr(dataType, "")).append(", ");
		sb.append("后续长度=").append(bytes2Int(dataLength)).append("->0x");
		sb.append(bytes2HexStr(dataLength, "")).append(", ");
		sb.append("定位信息=").append(bytes2HexStr(gnssData, "")).append("->0x");
		sb.append(bytes2HexStr(gnssData, "")).append("}");
		System.out.println(sb.toString());
	}
	
	/** 格式化头信息 */
	public String formatHeadInfo(P809 p809) {
		StringBuilder sb = new StringBuilder("{");
		sb.append("数据长度=").append(bytes2Int(p809.getLength())).append("->0x");
		sb.append(bytes2HexStr(p809.getLength(), "")).append(", ");
		
		sb.append("报文序号=").append(bytes2Int(p809.getSequeen())).append("->0x");
		sb.append(bytes2HexStr(p809.getSequeen(), "")).append(", ");
		
		sb.append("业务类型=").append(bytes2Short(p809.getType())).append("->0x");
		sb.append(bytes2HexStr(p809.getType(), "")).append(", ");
		
		sb.append("接入码=").append(bytes2Int(p809.getAcccode())).append("->0x");
		sb.append(bytes2HexStr(p809.getAcccode(), "")).append(", ");
		
		sb.append("版本号=").append(bytes2HexStr(p809.getVersion(), "")).append("->0x");
		sb.append(bytes2HexStr(p809.getVersion(), "")).append(", ");
		
		sb.append("是否加密=").append(p809.getPwdable()).append("->0x");
		sb.append(bytes2HexStr(new byte[]{p809.getPwdable()}, "")).append(", ");
		
		sb.append("加密参数=").append(bytes2Int(p809.getPwdparam())).append("->0x");
		sb.append(bytes2HexStr(p809.getPwdparam(), ""))
		;
		return sb.append("}").toString();
	}
	
	/** 加解密算法 */
	public void encrypt(byte[] data, int key) {
		if (key == 0) key = 1;
		int mkey = M1;
		if (0 == mkey) mkey = 1;
		
		int i = 0;
		while (i < data.length) {
			key = IA1 * (key % mkey) + IC1;
			data[i++] ^= ((key >> 20) & 0xFF);
		}
	}
	
	/** 转义 */
	public byte[] escape(byte[] data) {
		
		return data;
	}
	
	/** 字节数组转换为整型 */
	public int bytes2Int(byte b[]) {
		return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
	}
	
	public static byte[] int2Bytes(int n) {
		byte[] bs = new byte[4];
		bs[3] = (byte) (n & 0xff);
		bs[2] = (byte) (n >> 8 & 0xff);
		bs[1] = (byte) (n >> 16 & 0xff);
		bs[0] = (byte) (n >> 24 & 0xff);
		return bs;
	}
	
	/** 字节数组转换为短整型 */
	public short bytes2Short(byte[] b) {
		return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
	}
	
	/** 字节数组转换为16进制字符串 */
    public static String bytes2HexStr(byte[] bs, String split) {
    	if (null == bs || bs.length < 1) return null;
		StringBuilder sb = new StringBuilder();
		String stmp = "";
		for (byte b : bs) {
			stmp = Integer.toHexString(b & 0xFF);
			sb.append(stmp.length() == 1 ? "0"+stmp : stmp).append(split);
		}
		if(sb.length() > 0 && split.length() > 0) sb.deleteCharAt(sb.length()-1);
		return sb.toString().toUpperCase();
	}
    
    public static String byte2HexStr(byte b) {
    	String s = Integer.toHexString(b & 0xFF).toUpperCase();
    	if (s.length() == 1) s = "0"+s;
    	return s;
    }
    
    public static String int2BinaryStr(int i) {
    	String str = Integer.toBinaryString(i);
    	int len = str.length();
    	for (int j = 0; j < (32-len); j++) str = "0"+str;
    	return str;
    }
    
    public static String byte2BinaryStr(byte b) {
    	String str = Integer.toBinaryString(b);
    	int len = str.length();
    	for (int j = 0; j < (8-len); j++) str = "0"+str;
    	return str;
    }
    
    public String getStackTrace(Exception e) {
    	if (e == null) return ""; 
    	StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer,true));
		return writer.toString(); 
    }
	
	public static void main(String[] args) {
		P809Parser p = new P809Parser();
		int key = 1062831270;
		byte[] data = {0x29, (byte)0x85, 0x6B, 0x42, (byte)0xB1, (byte)0xCD, (byte)0xDA, 0x43, 
				(byte)0xEE, 0x7C, (byte)0xA5, 0x5F, 0x24, (byte)0xDE, 0x6D, 0x4E, (byte)0xAB, (byte)0xA9, 
				0x39, 0x2B, 0x55, 0x58, 0x1C, 0x07, 0x01, 0x0C, (byte)0x8A, (byte)0xF2, 0x52, 0x04, 0x31, 
				(byte)0x96, (byte)0x89, 0x55, 0x0C, 0x7A, 0x20, 0x54, (byte)0x98, (byte)0xEC, (byte)0xB6, 
				0x48, 0x5F, 0x4B, 0x24, (byte)0x84};
		
		System.out.print("原始：");
		for (int b : data) System.out.print(byte2HexStr((byte)b)+ " ");
		System.out.println();
		
		p.encrypt(data, key);
		System.out.print("加密：");
		for (int b : data) System.out.print(byte2HexStr((byte)b)+ " ");
		System.out.println();
		
		/*p.encrypt(data, key);
		System.out.print("解密：");
		for (int b : data) System.out.print(byte2HexStr((byte)b)+ " ");
		System.out.println();*/
		
		//正确输出应该是
		//0x03 0xD0 0x90 0x62 0x36 0x34 0x30 0x30 0x30 0x30 0x39 0x38 0x31 0x32 0x37 0x2E 0x30 
		//0x2E 0x30 0x2E 0x31 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 
		//0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x1F 0x42
	}
}
