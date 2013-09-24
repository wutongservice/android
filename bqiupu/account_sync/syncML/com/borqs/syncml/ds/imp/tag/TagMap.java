package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


//<!-- MAP operation. Create/Delete an item id map kept at the server. -->
//<!ELEMENT Map (CmdID, Target, Source, Cred?, Meta?, MapItem+)>
public class TagMap implements ITag {
	public String CmdID;
	public TagTarget Target;
	public TagSource Source;
	public TagCred Cred;
	public TagMeta Meta;
	public List<TagMapItem> MapItem;

	public TagMap(String CmdID, TagTarget Target, TagSource Source,
			TagCred Cred, TagMeta Meta, List<TagMapItem> MapItem) {
		this.CmdID = CmdID;
		this.Target = Target;
		this.Source = Source;
		this.Cred = Cred;
		this.Meta = Meta;
		this.MapItem = MapItem;
	}

	public String name() {
		return SyncML.Map;
	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(CmdID, encode);
		s += CaculateTagSize.getSize(Target, encode);
		s += CaculateTagSize.getSize(Source, encode);
		s += CaculateTagSize.getSize(Cred, encode);
		s += CaculateTagSize.getSize(Meta, encode);
		s += CaculateTagSize.getSize(MapItem, encode);

		return s;
	}
	
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
	}

	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Map);
		SyncmlXml.putTagText(writer, SyncML.CmdID, CmdID);
		Target.put(writer);
		Source.put(writer);
		if (Cred != null) {
			Cred.put(writer);
		}
		if (Meta != null) {
			Meta.put(writer);
		}
		for (TagMapItem item : MapItem) {
			item.put(writer);
		}
		writer.endTag(null, SyncML.Map);
	}
}
