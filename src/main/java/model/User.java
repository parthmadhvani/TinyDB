package model;

import java.util.List;

import utils.Logger;

import java.util.Arrays;

public class User 
{
    private String userID;
    private String password;
    private List<String> securityQuestions;
    private List<String> securityAnswers;

    public User(String userID, String password, List<String> securityQuestions, List<String> securityAnswers) 
    {
        this.userID = userID;
        this.password = password;
        this.securityQuestions = securityQuestions;
        this.securityAnswers = securityAnswers;
        Logger.logEvent("User created: " + userID);
    }

    public String getUserID() 
    {
        return userID;
    }

    public String getPassword() 
    {
        return password;
    }

    public List<String> getSecurityQuestions() 
    {
        return securityQuestions;
    }

    public List<String> getSecurityAnswers() 
    {
        return securityAnswers;
    }

    @Override
    public String toString() 
    {
    	Logger.logEvent("User serialized to string: " + userID);
        return userID + "###" + password + "###" + String.join(";", securityQuestions) + "###" + String.join(";", securityAnswers);
    }

    public static User fromString(String data) 
    {
        String[] parts = data.split("###", 4);
        String userID = parts[0];
        String password = parts[1];
        List<String> securityQuestions = Arrays.asList(parts[2].split(";"));
        List<String> securityAnswers = Arrays.asList(parts[3].split(";"));
        Logger.logEvent("User deserialized from string: " + userID);
        return new User(userID, password, securityQuestions, securityAnswers);
    }
}
