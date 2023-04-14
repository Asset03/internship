package com.example.internship.controller;



import com.example.internship.services.*;
import com.example.internship.repository.*;
import com.example.internship.controller.*;
import com.example.internship.models.*;

import org.dom4j.rule.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserAccountController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserAccountController() {
    }
    @GetMapping("/")
    public ModelAndView displayPage(ModelAndView modelAndView,HttpServletRequest req) {
        modelAndView.setViewName("page");
        return modelAndView;
    }

    @GetMapping("/register")
    public  ModelAndView displayRegistration(ModelAndView modelAndView, UserEntity userEntity){
        modelAndView.addObject("userEntity", userEntity);
        modelAndView.setViewName("register");
        return modelAndView;
    }
    @PostMapping("/register")
    public ModelAndView registerUser(ModelAndView modelAndView, UserEntity userEntity) {
        UserEntity existingUser = userRepository.findByEmailIdIgnoreCase(userEntity.getEmailId());
        if(existingUser != null) {
            modelAndView.addObject("message","This email already exists!");
            modelAndView.setViewName("error");
        } else {
            userEntity.setPassword(bCryptPasswordEncoder.encode(userEntity.getPassword()));
            userRepository.save(userEntity);
            ConfirmationToken confirmationToken = new ConfirmationToken(userEntity);
            confirmationTokenRepository.save(confirmationToken);

            //message
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userEntity.getEmailId());
            mailMessage.setSubject("Complete Registration!");
            mailMessage.setFrom("asetzhanedilov6@gmail.com");
            mailMessage.setText("To confirm your account, please click here : "
                    +"http://localhost:8080/confirm-account?token="+confirmationToken.getConfirmationToken());
            emailService.sendEmail(mailMessage);

            modelAndView.addObject("emailId", userEntity.getEmailId());
            modelAndView.setViewName("successfulRegistration");
        }
        return modelAndView;
    }



    @RequestMapping(value="/confirm-account", method= {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView confirmUserAccount(ModelAndView modelAndView, @RequestParam("token")String confirmationToken) {
        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
        if(token != null) {
            UserEntity user = userRepository.findByEmailIdIgnoreCase(token.getUserEntity().getEmailId());
            user.setEnabled(true);
            userRepository.save(user);
            modelAndView.setViewName("main");
        } else {
            modelAndView.addObject("message","The link is invalid or broken!");
            modelAndView.setViewName("error");
        }
        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView displayLogin(ModelAndView modelAndView,UserEntity userEntity){
        modelAndView.addObject("userEntity",userEntity);
        modelAndView.setViewName("login");
        return modelAndView;
    }
    @PostMapping("/login")
    public ModelAndView loginUser(ModelAndView modelAndView,UserEntity userEntity,Location locationEntity){
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getSession();
        UserEntity existUser = userRepository.findByEmailIdIgnoreCase(userEntity.getEmailId());

        if(existUser!=null && existUser.isEnabled() && bCryptPasswordEncoder.matches( userEntity.getPassword(),existUser.getPassword())){
            session.setAttribute("userDetails", existUser);
            modelAndView.addObject("locationEntity", locationEntity);
            return new ModelAndView("redirect:/main");
        }else{
            modelAndView.addObject("message","Invalid password or you're not confirmed email or this email not exist");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }

    @GetMapping("/main")
    public ModelAndView displayMain(ModelAndView modelAndView,Location locationEntity,HttpServletRequest request){
        UserEntity user = (UserEntity) request.getSession().getAttribute("userDetails");
        Set<Location> locations = user.getLocations();
        modelAndView.addAllObjects(Map.of("locationEntity",locationEntity,"locations",locations));
        modelAndView.setViewName("main");
        return modelAndView;
    }


    @PostMapping("/main")
    public ModelAndView createLocation(ModelAndView modelAndView, UserEntity userEntity, Location locationEntity, HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getSession().getAttribute("userDetails");
        locationEntity.setOwner(user);
        locationRepository.save(locationEntity);
        user.getLocations().add(locationEntity);
        return new ModelAndView("redirect:/main");
    }


    @DeleteMapping(value = "/main/{id}")
    @ResponseBody
    public ModelAndView removeLocation(ModelAndView modelAndView,Location locationEntity,HttpServletRequest request,@PathVariable("id") long id){
        locationRepository.deleteById(id);
        UserEntity user = (UserEntity) request.getSession().getAttribute("userDetails");
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getSession();
        session.setAttribute("userDetails",userRepository.findByEmailIdIgnoreCase(user.getEmailId()));
        return displayMain(modelAndView,locationEntity,request);
    }

    @GetMapping(value = "/main/{id}")
    @ResponseBody
    public ModelAndView displayFriend(ModelAndView modelAndView,HttpServletRequest request,@PathVariable("id") long id){
        UserEntity user = (UserEntity) request.getSession().getAttribute("userDetails");
        Location location = user.getLocation(id);


        List<UserEntity> users = (List<UserEntity>) userRepository.findAll();
        Set<UserEntity> friends = location.getFriends()==null?new HashSet<UserEntity>():location.getFriends();

        users= users.stream().filter(e->(!e.getEmailId().equals(user.getEmailId()) && !friends.contains(e))).collect(Collectors.toList());


        modelAndView.addAllObjects(Map.of("location",location,"users",users,"friends",friends));
        modelAndView.setViewName("friend");
        return modelAndView;
    }

    @PutMapping(value = "/main/{id}/add/{friend_email}")
    @ResponseBody
    public ModelAndView addFriend(ModelAndView modelAndView,HttpServletRequest request,@PathVariable("id") long id,@PathVariable("friend_email") String friend_email){
        UserEntity user = (UserEntity) request.getSession().getAttribute("userDetails");

        Location location = user.getLocation(id);

        Set<UserEntity> friends = location.getFriends()==null?new HashSet<UserEntity>():location.getFriends();

        friends.add(userRepository.findByEmailIdIgnoreCase(friend_email));
        location.setFriendsOwner(friends);
        locationRepository.save(location);
        userRepository.save(user);


        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getSession();
        session.setAttribute("userDetails",userRepository.findByEmailIdIgnoreCase(user.getEmailId()));
        return displayFriend(modelAndView,request,id);
    }

    @DeleteMapping(value = "/main/{id}/delete/{friend_email}")
    @ResponseBody
    public ModelAndView deleteFriend(ModelAndView modelAndView,Location locationEntity,HttpServletRequest request,@PathVariable("id") long id,@PathVariable("friend_email") String friend_email){

        UserEntity user = (UserEntity) request.getSession().getAttribute("userDetails");

        Location location = user.getLocation(id);

        Set<UserEntity> friends = location.getFriends()==null?new HashSet<UserEntity>():location.getFriends();

        friends.remove(userRepository.findByEmailIdIgnoreCase(friend_email));
        location.setFriendsOwner(friends);
        locationRepository.save(location);
        userRepository.save(user);

        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getSession();
        session.setAttribute("userDetails",userRepository.findByEmailIdIgnoreCase(user.getEmailId()));
        return displayFriend(modelAndView,request,id);
    }

}