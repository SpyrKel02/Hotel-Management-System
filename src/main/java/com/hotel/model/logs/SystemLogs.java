package com.hotel.model.logs;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="system_logs",schema = "iee2021065")
public class SystemLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="log_id")
    private int logID;

    @Column(name="action_type", nullable = false)
    private String actionType;

    @Column(name="action_description", nullable = false, columnDefinition = "TEXT")
    private String actionDescription;

    @Column(name="action_created_at",insertable = false, updatable = false)
    private LocalDateTime actionCreatedAt;

    @Column(name="tableName",nullable = false)
    private String tableName;

    public SystemLogs(){}

    public Integer getLogId(){return this.logID;}
    public String getActionType(){return this.actionType;}
    public String getActionDescription(){return this.actionDescription;}
    public LocalDateTime getActionCreatedAt(){return this.actionCreatedAt;}
    public String getTableName(){return this.tableName;}
}