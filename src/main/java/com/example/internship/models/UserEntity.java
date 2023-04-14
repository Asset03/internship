package com.example.internship.models;

import javax.persistence.*;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "users")
public class UserEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="user_id")
    private long userid;

    private String emailId;

    private String password;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    private boolean isEnabled;


    @OneToMany(mappedBy = "owner",targetEntity = Location.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private Set<Location> locations;



    @ManyToMany(mappedBy = "friendsOwner",fetch = FetchType.EAGER)
    private List<Location> friendsLocation;

    public UserEntity(long userid, String emailId, String password, String firstName, String lastName, boolean isEnabled, Set<Location> locations, List<Location> friendsLocation) {
        this.userid = userid;
        this.emailId = emailId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isEnabled = isEnabled;
        this.locations = locations;
        this.friendsLocation = friendsLocation;
    }

    public UserEntity() {


    }

    // getters and setters methods ///////////////////////////////
    public long getUserid() {
        return userid;
    }
    public void setUserid(long userid) {
        this.userid = userid;
    }
    public String getEmailId() {
        return emailId;
    }
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public Location getLocation(long id){
        return  locations.stream().filter(location -> location.getId()==id).findFirst().get();
    }

    public void setLocation(Set<Location> locations) {
        this.locations = locations;
    }
    public void removeLocation(Location location){
        this.locations.remove(location);
    }

}