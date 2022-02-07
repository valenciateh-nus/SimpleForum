package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import util.enumeration.AccessRightEnum;

/**
 *
 * @author valenciateh
 */
@Entity
public class ForumUser implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;

    private byte gender;
 
    @Temporal(TemporalType.DATE)
    private Date dob;

    @Temporal(TemporalType.DATE)
    private Date created;

    private AccessRightEnum accessRight; //0 for normal users, 1 for admins

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private ArrayList<Forum> forums;
    
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private ArrayList<ForumThread> threads;
    
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private ArrayList<ForumPost> posts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public AccessRightEnum getAccessRight() {
        return accessRight;
    }

    public void setAccessRight(AccessRightEnum accessRight) {
        this.accessRight = accessRight;
    }

    public ArrayList<Forum> getForums() {
        return forums;
    }

    public void setForums(ArrayList<Forum> forums) {
        this.forums = forums;
    }

    public ArrayList<ForumThread> getThreads() {
        return threads;
    }

    public void setThreads(ArrayList<ForumThread> threads) {
        this.threads = threads;
    }

    public ArrayList<ForumPost> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<ForumPost> posts) {
        this.posts = posts;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForumUser)) {
            return false;
        }
        ForumUser other = (ForumUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ForumUser[ id=" + id + " ]";
    }
    
}
