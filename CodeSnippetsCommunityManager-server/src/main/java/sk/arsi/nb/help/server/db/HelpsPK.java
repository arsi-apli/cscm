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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author arsi
 */
@Embeddable
public class HelpsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "IDHELPS")
    private int idhelps;
    @Basic(optional = false)
    @Column(name = "USERS_EMAIL")
    private String usersEmail;

    public HelpsPK() {
    }

    public HelpsPK(int idhelps, String usersEmail) {
        this.idhelps = idhelps;
        this.usersEmail = usersEmail;
    }

    public int getIdhelps() {
        return idhelps;
    }

    public void setIdhelps(int idhelps) {
        this.idhelps = idhelps;
    }

    public String getUsersEmail() {
        return usersEmail;
    }

    public void setUsersEmail(String usersEmail) {
        this.usersEmail = usersEmail;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idhelps;
        hash += (usersEmail != null ? usersEmail.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HelpsPK)) {
            return false;
        }
        HelpsPK other = (HelpsPK) object;
        if (this.idhelps != other.idhelps) {
            return false;
        }
        if ((this.usersEmail == null && other.usersEmail != null) || (this.usersEmail != null && !this.usersEmail.equals(other.usersEmail))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sk.arsi.nb.help.server.HelpsPK[ idhelps=" + idhelps + ", usersEmail=" + usersEmail + " ]";
    }

}
