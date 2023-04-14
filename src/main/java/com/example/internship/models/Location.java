package com.example.internship.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "location")
public class Location implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="location_id")
    private long id;



    private String name;


    @ManyToOne(fetch = FetchType.LAZY,optional = false)//cascadeType.all removed
    @JoinColumn(name = "user_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private UserEntity owner;









    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_friends_location",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private Set<UserEntity> friendsOwner;

    public Location() {
    }

    public Location(long id, String name, UserEntity owner, Set<UserEntity> friendsOwner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.friendsOwner = friendsOwner;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public void setFriendsOwner(Set<UserEntity> friendsOwner){
        this.friendsOwner=friendsOwner;
    }
    public Set<UserEntity> getFriends() {
        return friendsOwner;
    }



    @Override
    public String toString() {
        return name+id+"++++++++++++++";
    }
}
