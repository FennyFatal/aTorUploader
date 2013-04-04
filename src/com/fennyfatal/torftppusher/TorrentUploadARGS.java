package com.fennyfatal.torftppusher;

public class TorrentUploadARGS
{
	public String fileName;
	public String uploadURI;
	public TorrentUploadARGS(String fileName, String uploadURI)
	{
		this.fileName = fileName;
		this.uploadURI = uploadURI;
	}
}