package com.example.save02.engine;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.example.save02.domain.UpdateInfo;

public class UpdateParser {

	public static UpdateInfo getUpdateInfo(InputStream is) throws Exception {
		//get xml parser
		XmlPullParser xmlParse = Xml.newPullParser();
		UpdateInfo info = new UpdateInfo();
		xmlParse.setInput(is, "utf-8");
		int type = xmlParse.getEventType();
		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if("version".equals(xmlParse.getName())){
					String version = xmlParse.nextText();
					info.setVersion(version);
				}else if("description".equals(xmlParse.getName())){
					String description = xmlParse.nextText();
					info.setDescription(description);
				}else if("updateurl".equals(xmlParse.getName())){
					String apkurl = xmlParse.nextText();
					info.setUpdateurl(apkurl);
				}
				
				break;

			}

			type = xmlParse.next();
		}
		return info;
	}

}
