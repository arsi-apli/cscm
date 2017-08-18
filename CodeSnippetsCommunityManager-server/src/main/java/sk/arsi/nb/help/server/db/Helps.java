/*
 * (C) Copyright 2017 Arsi (http://www.arsi.sk/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package sk.arsi.nb.help.server.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import sk.arsi.nb.help.server.lucene.LuceneManager;

/**
 *
 * @author arsi
 */
@Entity
@Table(name = "HELPS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Helps.findAll", query = "SELECT h FROM Helps h"),
    @NamedQuery(name = "Helps.findByIdhelps", query = "SELECT h FROM Helps h WHERE h.idhelps = :idhelps"),
    @NamedQuery(name = "Helps.findByCreateddate", query = "SELECT h FROM Helps h WHERE h.createddate = :createddate"),
    @NamedQuery(name = "Helps.findByHelp", query = "SELECT h FROM Helps h WHERE h.help = :help"),
    @NamedQuery(name = "Helps.findByUser", query = "SELECT h FROM Helps h WHERE h.users = :users")})
public class Helps implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDHELPS")
    private Integer idhelps;
    @Column(name = "CREATEDDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createddate;
    @Column(name = "HELP", length = 5000)
    private String help;
    @Column(name = "DESCRIPTION", length = 100)
    private String description;
    @JoinTable(name = "KEYSLIST_has_HELPS", joinColumns = {
        @JoinColumn(name = "HELPS_IDHELPS", referencedColumnName = "IDHELPS")}, inverseJoinColumns = {
        @JoinColumn(name = "KEYSLIST_KEYNAME", referencedColumnName = "KEYNAME")})
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Keyslist> keyslistList = new ArrayList<>();
    @OneToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, mappedBy = "helpsIdhelps")
    private List<Ranks> ranksList = new ArrayList<>();

    @JoinTable(name = "CLASSESLIST_has_HELPS", joinColumns = {
        @JoinColumn(name = "HELPS_IDHELPS", referencedColumnName = "IDHELPS")}, inverseJoinColumns = {
        @JoinColumn(name = "CLASSESLIST_CLASSNAME", referencedColumnName = "CLASSNAME")})
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Classeslist> classeslistList = new ArrayList<>();
    @JoinColumn(name = "MIMETYPE_MIMETYPE", referencedColumnName = "MIMETYPE")
    @ManyToOne
    private Mimetype mimetype;
    @JoinColumn(name = "USERS_EMAIL", referencedColumnName = "EMAIL", updatable = false)
    @ManyToOne(optional = false)
    private Users users;

    public Helps() {
    }

    public Helps(Integer idhelps) {
        this.idhelps = idhelps;
    }

    public Integer getIdhelps() {
        return idhelps;
    }

    public void setIdhelps(Integer idhelps) {
        this.idhelps = idhelps;
    }

    public Date getCreateddate() {
        return createddate;
    }

    public void setCreateddate(Date createddate) {
        this.createddate = createddate;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public Users getUser() {
        return users;
    }

    public void setUser(Users user) {
        this.users = user;
    }

    @XmlTransient
    public List<Keyslist> getKeyslistList() {
        return keyslistList;
    }

    public void setKeyslistList(List<Keyslist> keyslistList) {
        this.keyslistList = keyslistList;
    }

    @XmlTransient
    public List<Classeslist> getClasseslistList() {
        return classeslistList;
    }

    public void setClasseslistList(List<Classeslist> classeslistList) {
        this.classeslistList = classeslistList;
    }

    @XmlTransient
    public List<Ranks> getRanksList() {
        return ranksList;
    }

    public void setRanksList(List<Ranks> ranksList) {
        this.ranksList = ranksList;
    }

    public Mimetype getMimetype() {
        return mimetype;
    }

    public void setMimetype(Mimetype mimetype) {
        this.mimetype = mimetype;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idhelps != null ? idhelps.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Helps)) {
            return false;
        }
        Helps other = (Helps) object;
        return !((this.idhelps == null && other.idhelps != null) || (this.idhelps != null && !this.idhelps.equals(other.idhelps)));
    }

    @Override
    public String toString() {
        return "com.apli.kafka.kafka.test.Helps[ idhelps=" + idhelps + " ]";
    }

    @PostPersist
    public void postPersist() {
        LuceneManager.addHelp(idhelps, help);
        LuceneManager.addDescription(idhelps, description);
    }

    @PostUpdate
    public void postUpdate() {
        LuceneManager.updateHelp(idhelps, help);
        LuceneManager.updateDescription(idhelps, description);
    }

    @PostRemove
    public void postRemove() {
        LuceneManager.removeHelp(idhelps, help);
        LuceneManager.removeDescription(idhelps, description);
    }
}
