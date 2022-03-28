package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dto.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller()
public class HomeController {
	
	@Autowired
	public BCryptPasswordEncoder passwordEncoder;
	@Autowired
	public UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "home-Smart Contact manager");
		
		return"home";
	}
	
	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register-Smart Contact manager");
		m.addAttribute("user",new User());
		return"signup";
	}
	
	
	
	//handler for registering user
	
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user")User user, BindingResult result1,
	@RequestParam(value="agreement",defaultValue="false")boolean agreement,Model model,HttpSession session) {
		
		
		
		try {
			
			if(!agreement) {
				System.out.println("you have not agreed the term and conditions");
			throw new Exception("you have not agreed the term and conditions");
			}
			
			
			if(result1.hasErrors()) {
				System.out.println("Errors"+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnable(true);
			user.setImageUrl("default.png");
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.println("USER"+user);
		User result = this.userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message",new Message("Successfully Registred !!","alert-success"));
			return"signup";
		}
		catch(Exception e){
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Something went to wrong!!"+e.getMessage(),"alert-danger"));
			return"signup";
		}
		
		
	}
	 // handler for signin
	  @GetMapping("/signin")
	   public String customLogin() {
						
			return"login";
			
					}
}
