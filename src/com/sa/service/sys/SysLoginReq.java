/**
 *
 * 项目名称：NettyServer
 * 类名称：SysLogin
 * 类描述：
 * 创建人：Y.P
 * 创建时间：2019年1月29日 下午2:37:02
 * 修改人：Y.P
 * 修改时间：2019年1月29日 下午2:37:02
 * @version  1.0
 *
 */
package com.sa.service.sys;

import com.sa.net.Packet;
import com.sa.net.PacketType;

public class SysLoginReq extends Packet {
	public SysLoginReq(){}

	@Override
	public void execPacket() {
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysLoginReq;
	}

}
