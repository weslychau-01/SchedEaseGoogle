package com.cs206.Team;

import java.time.LocalDateTime;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "team")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    @Id
    private String _id;
    private String teamName;
    private List<String> teamUserIds;
    private List<String> teamMeetingIds;
}