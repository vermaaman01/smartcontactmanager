package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	public boolean sendEmail(String subject,String message, String to) {
		//rest of the code
		boolean f = false;
		String from="pradeepnishad354@gmail.com";
		
		//Variable for gmail
		String host="smtp.gmail.com";
		
		//get the system properties 
		Properties properties=System.getProperties();
		System.out.println("PROPERTIES"+properties);
		
		
		//setting important information to properties object
		
		//host set
		
		properties.put("mail.smpt.host",host);
		properties.put("mail.smpt.host","465");
		properties.put("mail.smpt.ssl.enable","false");
		properties.put("mail.smpt.auth","true");
		
		
		//step1: to get the session object..
		Session session=Session.getInstance(properties, new Authenticator(){
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
			
				return new PasswordAuthentication("pradeepnishad354@gmail.com","Swadeep@123");
			}
			
		});
		
		session.setDebug(true);
		
		//step2;compose the message[text,multi media]
		
		MimeMessage m=new MimeMessage(session);
		
		try {
			//from email
			m.setFileName(from);
			
			//adding recipient to message
			
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding text to message
			m.setText(message);
			
			//send
			
			// step 3:send the message using Transport class
			Transport.send(m);
			System.out.println("send success........");
			f=true;
			
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		return f;
		
		
		
		
		
	}

}
