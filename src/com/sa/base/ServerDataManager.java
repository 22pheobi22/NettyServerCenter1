/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.base]
 * 类名称: [ServerDataManager]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月15日 上午11:15:22]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月15日 上午11:15:22]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.sa.base.element.ChatLog;
import com.sa.base.element.Logs;
import com.sa.base.element.People;
import com.sa.base.element.Room;
import com.sa.base.element.Share;
import com.sa.util.Constant;

public class ServerDataManager {
	/** 缓存房间信息 String:roomId*/
	private Map<String, Room> ROOM_INFO_MAP  = new ConcurrentHashMap<>();

	/** 记录 房间 空余 时长 string:roomId Integer:次数(一次五分钟)*/
	private Map<String, Integer> ROOM_FREE_MAP = new ConcurrentHashMap<>();

	/** 获取 房间 空余 时长 */
	public Integer getFreeRoom(String roomId) {
		int freeNum = 0;

		if (null == ROOM_FREE_MAP.get(roomId)) {
			ROOM_FREE_MAP.put(roomId, 0);
		}

		freeNum = ROOM_FREE_MAP.get(roomId);

		return freeNum;
	}

	/**
	 * 获取共享
	 */
	public Map<String, Share> getShare(String roomId) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		return room.getShare();
	}

	/**
	 * 获取共享
	 */
	public Object getShare(String roomId, String key) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		Share share = room.getShare().get(key);
		if (null == share) {
			return null;
		}


		return share.getContent();
	}

	/**
	 * 获取共享
	 */
	public List<Object> getShareList(String roomId, String key) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		Share share = room.getShare().get(key);
		if (null == share) {
			return null;
		}

		return share.getListContent();
	}

	/**
	 * 设置共享
	 */
	public void setShare(String roomId, String key, String value, String type) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		Map<String, Share> shareMap = room.getShare();
		
		Share share = shareMap.get(key);
		if (null == share) {
			share = new Share(type);
			shareMap.put(key, share);
		}
		
		if ("1".equals(share.getType())) {
			share.setContent(value);
		} else if ("n".equals(share.getType())) {
			if(null!=value){
				String[] values = value.split("##");
				for (String v : values) {
					share.add(v);
				}
			}
			
		}
	}
	
	/**
	 * 更新共享
	 */
	public int updateShare(String roomId, String key, String value,int index) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		int rs =0;
				
		Share share = room.getShare().get(key);
		if(null!=share){
			rs = share.updListContent(index,value);
		}

		return rs;
	}
	
	/**
	 * 更新共享
	 */
	public int updateShare(String roomId, String key, String oldValue,String newValue) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		int rs =0;
		
		Share share = room.getShare().get(key);
		if(null!=share){
			rs = share.updListContent(oldValue,newValue);
		}

		return rs;
	}

	/**
	 * 移出共享
	 */
	public int removeShare(String roomId, String key, String value) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		int rs = room.getShare().get(key).removeListContent(value);

		return rs;
	}

	/**
	 * 移出共享
	 */
	public Object removeShare(String roomId, String key) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		Object obj = room.getShare().remove(key);

		return obj;
	}

	/**
	 * 移出共享
	 */
	public int removeShare(String roomId, String key, int index, int len) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		int rs = room.getShare().get(key).removeListContent(index, len);

		return rs;
	}

	public void removeShare(String roomId, String key, String[] arr) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		Share share = room.getShare().get(key);

		Object[] index = sort_asc(arr);
		for (int i = index.length-1; i>=0; i--) {
			if (share.getListContent().size() > (int) index[i]) {
				share.removeListContent((int) index[i]);
			}
		}

	}

	private Object[] sort_asc(String[] arr) {
		Set<Integer> index = new TreeSet<Integer>();
		
		for (int i=0; i<arr.length; i++) {
			if (null == arr[i] || "".equals(arr[i])) continue;

			index.add(Integer.parseInt(arr[i]));
		}
		return index.toArray();
	}

	/**
	 * 注销聊天室
	 */
	public Room removeRoom(String roomId) {

		Room room = ROOM_INFO_MAP.remove(roomId);
		
		return room;
	}

	/**
	 * 移出聊天室
	 */
	public synchronized void removeRoomUser(String userId) {
		// 便利房间信息Map
		for (Entry<String, Room> entry : ROOM_INFO_MAP.entrySet()) {
			// 如果房间里有符合userid的人
			if (null != entry.getValue().getPeoples().get(userId)) {
				//将用户从聊天室踢出
				removeRoomUser(entry.getKey(), userId);
			}
		}
	}

	/**
	 * 移出聊天室
	 */
	public synchronized People removeRoomUser(String roomId, String userId) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		// 获取根据userid被成功删除的人员信息
		People people = room.getPeoples().remove(userId);
		// 如果人员信息不为空
		if (null != people) {
			// 设置房间内人数-1
			room.setPeopleNum(room.getPeopleNum()-1);
		}

		this.print("removeRoomUser");

		return people;
	}

	/**
	 * 禁言
	 */
	public People notSpeakAuth(String roomId, String userId) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		// 根据userId获取用户信息
		People people = room.getPeoples().get(userId);
		// 如果用户不为空
		if (null != people) {
			// 设置禁言
			people.getAuth().put(Constant.AUTH_SPEAK, 0);
			room.getPeoples().put(userId, people);

			room.getNotSpeakPeoples().put(userId, 0);
			System.out.println("userId:"+userId+"==auth:"+people.getAuth().get(Constant.AUTH_SPEAK));
		}
		return people;
	}

	/**
	 * 移出禁言
	 */
	public People speakAuth(String roomId, String userId) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		// 根据userId获取用户信息
		People people = room.getPeoples().get(userId);
		// 如果人员信息不为空
		if (null != people) {
			// 设置用户禁言状态
			people.getAuth().put(Constant.AUTH_SPEAK, 1);
			room.getNotSpeakPeoples().remove(userId);
		}

		return people;
	}

	/**
	 * 移出房间权限
	 */
	public void removeRoomAuth(String roomId, HashSet<String> roomRoles) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		// 遍历房间内人员信息Map
		for (Map.Entry<String, People> entry : room.getPeoples().entrySet()) {
			// 获取人员角色
			HashSet<String> roleHS = entry.getValue().getRole();
			// 遍历角色集合
			for(Iterator<?> it=roleHS.iterator();it.hasNext();) {
				// 如果人员角色符合房间禁言规则
				if (roomRoles.contains(it.next())) {
					// 解除禁言
					entry.getValue().getAuth().remove(Constant.AUTH_SPEAK);
					room.getNotSpeakPeoples().remove(entry.getKey());
				}
			}
		}
		//删除禁止操作角色权限
		room.getNotDoRole().removeAll(roomRoles);
	}

	/**
	 * 设置房间禁言权限
	 *
	 * HashSet<String> roomRoles  房间被禁言角色
	 */
	public void setRoomRole(String roomId, HashSet<String> roomRoles, boolean notSpeak) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		// 设置初始化禁止操作角色
		room.setNotDoRole(roomRoles);
		// 设置初始化禁止操作角色
		room.getNotDoRole().add("role");
		// 遍历房间内人员信息
		for (Map.Entry<String, People> entry : room.getPeoples().entrySet()) {
			// 获取人员角色
			HashSet<String> peopleRole = entry.getValue().getRole();

			boolean flag = true;
			// 遍历用户角色
			for(Iterator<?> it=peopleRole.iterator();it.hasNext();) {
				// 如果人员角色包含禁言角色
				if (roomRoles.contains(it.next())) {
					//设置禁言
					entry.getValue().getAuth().put(Constant.AUTH_SPEAK, 0);
					room.getNotSpeakPeoples().put(entry.getKey(), 0);
					flag = false;
				}
			}

			if (flag || !notSpeak) {
				entry.getValue().getAuth().put(Constant.AUTH_SPEAK, 1);
				room.getNotSpeakPeoples().remove(entry.getKey());
			}

		}
	}

	/**
	 * 获取房间
	 */
	public Room getRoom(String roomId) {
		// 根据roomId获取房间信息
		Room room = ROOM_INFO_MAP.get(roomId);
		// 如果没有该房间
		if (null == room) {
			// 实例化一个房间
			room = new Room();
		}
		// 创建房间
		ROOM_INFO_MAP.put(roomId, room);

		return room;
	}

	public Set<String> getRooms() {
//		Set<String> keys = ROOM_INFO_MAP.keySet();
		Set<String> keys = new HashSet<>();
		for (Map.Entry<String, Room> m : ROOM_INFO_MAP.entrySet()) {
			keys.add(m.getKey()+"["+m.getValue().getPeopleNum()+"]");
		}
		return keys;
	}
	/**
	 * 创建房间
	 * 向房间内添加人员及人员角色、权限
	 */
	public synchronized void setRoomUser(String roomId, String userId, String name, String icon, String agoraId, HashSet<String> userRole, boolean notSpeak) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		// 获取房间内人员信息Map
		Map<String, People> peopleMap = room.getPeoples();
		// 根据userId获取用户信息
		People people = peopleMap.get(userId);
		// 如果人员信息为空
		if (null == people) {
			// 实例化人员信息
			people = new People();
		}
		// 设置说话权限
		people.setIcon(icon);
		people.setName(name);
		people.setAgoraId(agoraId);
		people.getAuth().put(Constant.AUTH_SPEAK, 1);
		if (null != room.getNotSpeakPeoples().get(userId)) {
			people.getAuth().put(Constant.AUTH_SPEAK, 0);
		}
		// 获取房间禁言角色
		HashSet<String> notDoRole = room.getNotDoRole();
		// 如果禁言角色为空
		if (null == notDoRole) {
			// 如果禁止发言
			if (notSpeak) {
				// 设置用户为禁止发言
				people.getAuth().put(Constant.AUTH_SPEAK, 0);
				room.getNotSpeakPeoples().put(userId, 0);
			}
		} else {
			// 遍历用户角色
			for(Iterator<?> it=userRole.iterator();it.hasNext();) {
				// 如果禁言角色包含用户角色
				if (notDoRole.contains(it.next())) {
					// 设置用户为禁止发言
					people.getAuth().put(Constant.AUTH_SPEAK, 0);
					room.getNotSpeakPeoples().put(userId, 0);
				}
			}
		}

		// 将用户角色添加到人员角色信息中
		people.getRole().addAll(userRole);
		// 向房间内添加用户及相关信息
		peopleMap.put(userId, people);
		// 设置房间用户数量增量+1
		room.setPeopleNum(room.getPeopleNum()+1);

		this.print("setRoomUser");
	}

	/**
	 * 创建房间
	 * 向房间内添加人员及人员角色、权限
	 */
	public synchronized void setRoomUser(String roomId, String userId, String name, String icon, HashSet<String> userRole, boolean notSpeak) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		// 获取房间内人员信息Map
		Map<String, People> peopleMap = room.getPeoples();
		// 根据userId获取用户信息
		People people = peopleMap.get(userId);
		// 如果人员信息为空
		if (null == people) {
			// 实例化人员信息
			people = new People();
		}
		// 设置说话权限
		people.setIcon(icon);
		people.setName(name);
		people.getAuth().put(Constant.AUTH_SPEAK, 1);
		if (null != room.getNotSpeakPeoples().get(userId)) {
			people.getAuth().put(Constant.AUTH_SPEAK, 0);
		}
		// 获取房间禁言角色
		HashSet<String> notDoRole = room.getNotDoRole();
		// 如果禁言角色为空
		if (null == notDoRole) {
			// 如果禁止发言
			if (notSpeak) {
				// 设置用户为禁止发言
				people.getAuth().put(Constant.AUTH_SPEAK, 0);
				room.getNotSpeakPeoples().put(userId, 0);
			}
		} else {
			// 遍历用户角色
			for(Iterator<?> it=userRole.iterator();it.hasNext();) {
				// 如果禁言角色包含用户角色
				if (notDoRole.contains(it.next())) {
					// 设置用户为禁止发言
					people.getAuth().put(Constant.AUTH_SPEAK, 0);
					room.getNotSpeakPeoples().put(userId, 0);
				}
			}
		}

		// 将用户角色添加到人员角色信息中
		people.getRole().addAll(userRole);
		// 向房间内添加用户及相关信息
		peopleMap.put(userId, people);
		// 设置房间用户数量增量+1
		room.setPeopleNum(room.getPeopleNum()+1);

		this.print("setRoomUser");
	}

	/**
	 * 获取人员列表
	 */
	public Map<String, People> getRoomUesrs(String roomId) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		this.print("getRoomUesrs");

		return room.getPeoples();
	}
	
	/**
	 * 获取普通教师列表
	 */
	public Map<String, People> getRoomTeachers(String roomIds) {
		Map<String, People> teachers = new HashMap<String, People>();
		if (null == roomIds || "".equals(roomIds)) {
			return teachers;
		}

		String[] rooms = roomIds.split(",");
		for (String roomId : rooms) {
			Room room = this.getRoom(roomId);
			Map<String, People> peoples = room.getPeoples();

			for (Entry<String, People> entry : peoples.entrySet()) {
				if(entry.getValue().getRole().contains(Constant.ROLE_TEACHER)){
					entry.getValue().setUserId(entry.getKey());
					teachers.put(roomId, entry.getValue());

					break;
				}
			}
		}
		return teachers;
	}

	/**
	 * 获取人员
	 */
	public People getRoomUesr(String roomId, String userId) {
		// 获取房间内人员信息Map
		Map<String, People> peopleMap = getRoomUesrs(roomId);
		// 根据userId获取用户信息
		People people = peopleMap.get(userId);

		return people;
	}

	/**
	 * 设置房间人员自定义权限
	 * 
	 * @param roomId		房间ID
	 * @param userId		用户ID
	 * @param roleCode		权限CODE
	 * @param flag			操作 (+：添加 -:删除)
	 * @param num			多人或单人权限标识（1、n）
	 */
	public void setRoomUserDefAuth(String roomId, String userId, String roleCode, String flag, String num) {
		// 获取房间内人员信息Map
		Map<String, People> peopleMap = getRoomUesrs(roomId);

		// 根据userId获取用户信息
		People people = peopleMap.get(userId);
		// 如果人员信息不为空
		if (null != people) {
			if ("+".equals(flag)) {
				if ("1".equals(num)) {
					for (Entry<String, People> entry : peopleMap.entrySet()) {
						entry.getValue().getAuth().remove(roleCode);
					}
				}

				people.getAuth().put(roleCode, 1);
			} else if ("-".equals(flag)) {
				people.getAuth().remove(roleCode);
			}
		}
	}

	/**
	 * 获取人员角色
	 */
	public HashSet<String> getRoomUesrRole(String roomId, String userId) {
		// 获取房间内人员信息Map
		Map<String, People> peopleMap = getRoomUesrs(roomId);
		// 根据userId获取用户信息
		People people = peopleMap.get(userId);
		// 如果人员信息不为空
		if (null != people) {
			return people.getRole();
		}

		return null;
	}

	/**
	 * 获取人员权限
	 */
	public HashMap<String, Integer> getRoomUesrAuth(String roomId, String userId) {
		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);
		// 获取房间内人员信息Map
		Map<String, People> peopleMap = room.getPeoples();
		// 根据userId获取用户信息
		People people = peopleMap.get(userId);
		// 如果人员信息不为空
		if (null != people) {
			return people.getAuth();
		}

		return null;
	}

	/**
	 * 获取人员存在的房间
	 */
	public String getUserRoomNo(String userId) {
		String roomId = "";
		// 遍历房间信息
		for (Entry<String, Room> entry : ROOM_INFO_MAP.entrySet()) {
			// 如果如果用户信息不为空
			if (null != entry.getValue().getPeoples().get(userId)) {
				roomId +=(entry.getKey()+",");
			}
		}

		return roomId.equals("")?null:roomId;
	}

	/** 获取 房间id 和 每个房间人数*/
	public Map<String, Integer> getRoomInfo() {
		Map<String, Integer> roomInfoMap = new HashMap<String, Integer>();
		for (Entry<String, Room> entry : ROOM_INFO_MAP.entrySet()) {

			String roomId = entry.getKey();

			Room room = entry.getValue();

			int peopleNum = room.getPeopleNum();

			roomInfoMap.put(roomId, peopleNum);
		}
		return roomInfoMap;
	}

	/**
	 * 获取聊天记录列表
	 * String roomId 房间id
	 * String chatKey 聊天记录key 时间+事务id 逗号分隔
	 * int chatNum   获取聊天记录数量
	 */
	public List<Logs> getRoomChats(String roomId, String chatKey, int chatNum) {

		boolean check = true;

		// 根据roomid获取房间信息
		Room room = this.getRoom(roomId);

		ChatLog chatLog = room.getChatLog();

		List<Logs> logsList = new ArrayList<Logs>();

		TreeMap<String, Long> logPostion = chatLog.getLogPostion();

		if (null == logPostion) {
			check = false;
		}

		if (check) {
			Long index = (long) chatLog.getLogsList().size();
			if (!chatKey.equals("0")) { // 第一次获取历史消息
				if (null == logPostion.get(chatKey)) {
					check = false;
				} else {
					index = logPostion.get(chatKey);
				}
			}

//			if (check) {
//				for (int i = index; i > (index - chatNum) && size>0 && i>=0; i--) {
//					if (0 <= i && null != chatLog.getLogsList().get(i)) {
//						logsList.add(chatLog.getLogsList().get(i));
//					}
//				}
//			}
			if (check) {
				long begin = 0;
				if (index - chatNum < 0 ) {
					begin = 0;
				} else {
					begin = index-chatNum;
				}

				for (long i = begin; i<index; i++) {
					if (0 <= i && null != chatLog.getLogsList().get((int) i)) {
						logsList.add(chatLog.getLogsList().get((int) i));
					}
				}
			}
		}

		return logsList;
	}

	/** 设置消息缓存*/
	public synchronized void setRoomChats(String roomId, String chatKey, String userId, String msg) {

		Room room = this.getRoom(roomId);

		ChatLog chatLog = room.getChatLog();

		if (null == chatLog) {
			chatLog = new ChatLog();
			room.setChatLog(chatLog);
		}

		List<Logs> logsList = chatLog.getLogsList();

		if (null == logsList) {
			logsList = new ArrayList<Logs>();
			chatLog.setLogsList(logsList);
		}

		TreeMap<String, Long> logsPostion = chatLog.getLogPostion();

		if (null == logsPostion) {
			logsPostion = new TreeMap<String, Long>();
			chatLog.setLogPostion(logsPostion);
		}

		try {
			logsPostion.put(chatKey, (long) logsList.size());
		} catch (Exception e) {
			System.out.println("chatKey = " + chatKey + "\r\nlogsList = " + (null == logsList ? "null" : logsList.size())
							+ "\r\nlogsPostion = " + (null == logsPostion ? "null" : logsPostion.size()));
			e.printStackTrace();
		}

		People people = this.getRoomUesr(roomId, userId);

		Long sendTime = Long.parseLong(chatKey.substring(0,chatKey.indexOf(",")));

		int transactionId = Integer.parseInt(chatKey.substring(chatKey.indexOf(",")+1));

		Logs logs = new Logs(userId, people.getIcon(), people.getName(), msg, sendTime, transactionId);

		logsList.add(logs);
	}

	/** 清除历史消息记录*/
	public void cleanLogs(String roomId) {
		Room room = this.getRoom(roomId);
		if (null != room) {
			ChatLog chatLog = room.getChatLog();
			if (null != chatLog) {
				if (null != chatLog.getLogsList()) {
					chatLog.getLogsList().clear();
				}
				if (null != chatLog.getLogPostion()) {
					chatLog.getLogPostion().clear();
				}
			}
		}
	}

	/**
	 * 同一用户不能访问方式的数量
	 */
	public int getRoomTheSameUserCannotAccessNum(String roomId, String userId) {
		/*if (userId.endsWith("APP")) {
			userId = userId.replace("APP", "");
		}*/

		Map<String, People> peoples = this.getRoomUesrs(roomId);
		int num = 0;
		for (Map.Entry<String, People> people : peoples.entrySet()) {
			if (people.equals(userId)) {
				num++;
			}
		}

		return num;
	}

	public void print(String method) {
//		for (Entry<String, Room> entry : ROOM_INFO_MAP.entrySet()) {
//			System.out.println(method + "\tROOM ID : " + entry.getKey());
//
//			for(Iterator<?> it=entry.getValue().getNotDoRole().iterator();it.hasNext();) {
//				System.out.print(it.next() + " ");
//			}
//
//			System.out.println("people num : " + entry.getValue().getPeopleNum());
//
//			for (Entry<String, People> p : entry.getValue().getPeoples().entrySet()) {
//				System.out.println(p.getKey() + "\t" + p.getValue().getName());
//			}
//
//			System.out.println("chat num : " + entry.getValue().getChatLog().getLogsList().size());
//		}
//		System.out.println("------------------------------------------------------------------------");
	}

	public void setFreeRoom(String roomId, int freeNum) {
		freeNum++;
		ROOM_FREE_MAP.put(roomId, freeNum);
	}

	public void cancelFreeRoom(String roomId) {
		ROOM_FREE_MAP.remove(roomId);
	}

}
