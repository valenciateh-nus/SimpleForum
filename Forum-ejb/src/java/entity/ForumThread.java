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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import util.enumeration.ThreadStatus;

/**
 *
 * @author valenciateh
 */
@Entity
public class ForumThread implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private ThreadStatus status;
    
    private int replies = 0;
    
    @Temporal(TemporalType.DATE)
    private Date created;
    
    @Temporal(TemporalType.DATE)
    private Date updated;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private ArrayList<ForumPost> posts;
    
    @ManyToOne
    private Forum forum;
    
    @ManyToOne
    private ForumUser user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReplies() {
        return replies;
    }

    public void setReplies(int replies) {
        this.replies = replies;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public ArrayList<ForumPost> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<ForumPost> posts) {
        this.posts = posts;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public ForumUser getUser() {
        return user;
    }

    public void setUser(ForumUser user) {
        this.user = user;
    }

    public ThreadStatus getStatus() {
        return status;
    }

    public void setStatus(ThreadStatus status) {
        this.status = status;
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
        if (!(object instanceof ForumThread)) {
            return false;
        }
        ForumThread other = (ForumThread) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Thread[ id=" + id + " ]";
    }
    
}
