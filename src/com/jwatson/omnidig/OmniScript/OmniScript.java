package com.jwatson.omnidig.OmniScript;

import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;

public class OmniScript {
	
	enum Script {
			type,
			typename,
			body_open,
			body_close,
			variabe,
			assign,
			semicolon;
	}
	
	//final String regex = "\\s*(\\w)\\s?(\\w)\\s*\\{(.*)\\};";
	final String regex = "(\\w+)\\s+?(\\w+).*?\\{(.*?)\\};";
	public static OmniScript instance;
	
	Pattern pattern;
	Matcher matcher;
	
	public OmniScript() {
		
		//ParseFile("player.txt");
		
	}
	
	public void ParseFile(String filename) {
		
		try {
		
		FileHandle file = Gdx.files.getFileHandle(filename, FileType.Local);
		
		InputStreamReader in = new InputStreamReader(file.read(), "UTF8");
		
		CharBuffer buf = CharBuffer.allocate(1024);
		
		in.read(buf);
		in.close();
		
		buf.flip();
		ParseScript(buf.toString());
		
		
		}
		catch(Exception e) {
			Gdx.app.debug("Script error", ""+e.toString());
		}
		
	}
	
	public static void startScriptEngine() {
		instance = new OmniScript();
	}
	
	
	
	public void ParseScript(String script) {
		
		matcher = Pattern.compile(regex,Pattern.DOTALL).matcher(script);
		matcher.find();
		matcher.find();
		Gdx.app.debug("", ""+matcher.group(2));
	}
	

}
