package com.fennyfatal.torftppusher;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.entity.mime.content.ContentBody;

public class MyContentBody implements ContentBody {

	byte[] body;
	String MimeType;
	String fileName;
	public MyContentBody(byte[] body, String MimeType, String fileName) {
		this.body = body;
		this.MimeType = MimeType;
		this.fileName = fileName;
	}
	@Override
	public String getCharset() {
		return "8bit";
	}

	@Override
	public long getContentLength() {
		return body.length;
	}

	@Override
	public String getMediaType() {
		return "";
	}

	@Override
	public String getMimeType() {
		return MimeType;
	}

	@Override
	public String getSubType() {
		return "";
	}
	@Override
	public String getTransferEncoding() {
		return "";
	}

	@Override
	public String getFilename() {
		return fileName;
	}

	@Override
	public void writeTo(OutputStream arg0) throws IOException {
		for (int i =0; i<body.length;i++)
			arg0.write(body[i]);
		
	}

}
