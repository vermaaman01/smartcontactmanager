package com.smart.controller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dto.ContactRepository;
import com.smart.dto.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder; 
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired 
	private ContactRepository contactRepository;
	
	
	//method for adding common data to response
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName=principal.getName();
		System.out.println("USERNAME "+userName);
		//get the user using username(email)
		
		User user=userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		
		model.addAttribute("user",user);
	}
	
	//dashbord home
	@RequestMapping("/index")
	public String dashboard(Model model ,Principal principal) {
	model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	
	//open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processcontact(@ModelAttribute Contact contact,
			
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session) {
		
		try {
		String name=principal.getName();
		
		User user=this.userRepository.getUserByUserName(name);
		
		//processing and uploading file
		if(file.isEmpty()) {
			
			//if file is empty then try our message
			
			contact.setImage("contact.jpg");
			
		}
		else {
			//file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile=new ClassPathResource("static/img").getFile();
			
		Path path=	Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("image is uploaded successfully");
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("DATA "+contact);
		
		System.out.println("added to database");
		
		//message success...
		session.setAttribute("message",new Message("your contact is added !!Add more..","success "));
		}catch(Exception e){
			
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			//message error 
			session.setAttribute("message",new Message("something went to wrong try again","danger "));
		}
		
		return "normal/add_contact_form";
	}
	
	
	//show contacts
	
	@GetMapping("/show-contacts")
	public String showContacts(Model m, Principal principal) {
		m.addAttribute("title","Show user Contacts");
		
		String userName=principal.getName();
	User user = this.userRepository.getUserByUserName(userName);
List<Contact> contacts=	this.contactRepository.findContactsByUser(user.getId());
m.addAttribute("contacts",contacts);
		
	
		
		return "normal/show_contacts";
	}
	
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,
			Principal principal)
	{

		
		
		System.out.println("CID"+cId);
		Contact contact=this.contactRepository.findById(cId).get();

//		System.out.println("contact"+contact.getcId());
		
		
	User user=	this.userRepository.getUserByUserName(principal.getName());
	user.getContacts().remove(contact);
	
	this.userRepository.save(user);
	
	
		System.out.println("DELETED");
		
		session.setAttribute("message", new Message("Contact deleted successfully","success"));
		return "redirect:/user/show-contacts";
	}
	
	
// open update form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		m.addAttribute("title","Update Contact" );
		
	Contact contact =	this.contactRepository.findById(cid).get();
	m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	
	//update contact handler
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model m,HttpSession session,Principal principal) {
		
		try {
			//old Contact details
		Contact oldcontactDetail=	this.contactRepository.findById(contact.getcId()).get();
			
			
			
			//image
			
			
			if(!file.isEmpty()) {
				
				
				
				
				//update new photo
				
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=	Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
					contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldcontactDetail.getImage());
			}
			
			
	User user = this.userRepository.getUserByUserName(principal.getName());
	contact.setUser(user);
	this.contactRepository.save(contact);
	
	session.setAttribute("message",new Message("Your contact is updated","success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME"+contact.getName());
		
		System.out.println("CONTACT ID"+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}

	// showing particular details
	 
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		
		System.out.println("CID"+cId);
		
	Optional<Contact> contactOptional=	this.contactRepository.findById(cId);
Contact contact = contactOptional.get();

String userName=principal.getName();
User user=this.userRepository.getUserByUserName(userName);


if(user.getId() == contact.getUser().getId())
{
model.addAttribute("contact",contact);
model.addAttribute("title",contact.getName());
}	
		return "normal/contact_detail";
	}
	
	
	//your profile handler
	
	@GetMapping("/profile")
	public String yourProfile() {
		
		return "normal/profile";
	}
	
	
//open settings Handler
	
	@GetMapping("/settings")
	public String openSetting() {
		
		return "normal/settings";
	}
	
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		System.out.println("OLD PASSWORD "+oldPassword);
		System.out.println("NEW PASSWORD "+newPassword);
		
	String userName	=principal.getName();
User currentUser=this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change password
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			
			session.setAttribute("message",new Message("Your password is successfully changed...","success"));
			
		}else {
			
			//error...
			
			
		
		session.setAttribute("message",new Message("Please enter correct old password...","danger"));
		
		return "redirect:/user/settings";
		
		}
		
		return "redirect:/user/index";
		
	}
}


