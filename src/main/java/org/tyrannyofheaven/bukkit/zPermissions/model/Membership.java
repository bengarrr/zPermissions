/*
 * Copyright 2011 Allan Saddi <allan@saddi.com>
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
 */
package org.tyrannyofheaven.bukkit.zPermissions.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.annotation.CacheStrategy;

/**
 * Represents group membership.
 * 
 * @author asaddi
 */
@Entity
@Table(name="memberships")
@UniqueConstraint(columnNames={"member", "group_id"})
@CacheStrategy(useBeanCache=true)
public class Membership {

    private Long id;

    private String member;

    private PermissionEntity group;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    @Column(name="group_id")
    @ManyToOne(optional=false)
    public PermissionEntity getGroup() {
        return group;
    }

    public void setGroup(PermissionEntity group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Membership)) return false;
        Membership o = (Membership)obj;
        return getMember().equals(o.getMember()) &&
            getGroup().equals(o.getGroup());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + getMember().hashCode();
        result = 37 * result + getGroup().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Membership[%s -> %s]", getGroup().getName(), getMember());
    }

}
