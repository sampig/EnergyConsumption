package org.zhuzhu.energyconsumption.scanner.model;

import org.zhuzhu.energyconsumption.scanner.utils.CommonUtils;

public class DeviceModel {

	public String deviceID;
	public String webserver;
	public boolean direct;

	public static DeviceModel generateDevice() {
		DeviceModel dm = new DeviceModel();
		dm.deviceID = CommonUtils.getRandomHex(16);
		dm.webserver = CommonUtils.getDefaultURL();
		dm.direct = false;
		return dm;
	}

}
