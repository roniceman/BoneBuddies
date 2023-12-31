package com.example.bonebuddies;

public class User
{
    protected String email;
    protected String firstName;
    protected String lastName;
    protected String profile_img_uri;

    public User(String email, String firstName, String lastName, String imageUrl) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profile_img_uri = profile_img_uri;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getProfile_img_uri() {
        return profile_img_uri;
    }

    public void setProfile_img_uri(String profile_img_uri) {
        this.profile_img_uri = profile_img_uri;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", profile_img=" + profile_img_uri +
                '}';
    }
}
