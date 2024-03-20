package com.cs206.Team;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    public void saveMeetingId(String meetingId, Team team){
        Set<String> teamMeetingIds = team.getTeamMeetingIds();
        teamMeetingIds.add(meetingId);
        team.setTeamMeetingIds(teamMeetingIds);
        teamRepository.save(team);
    }

    public void saveAllMeetingId(List<String> meetingIds, Team team){
        Set<String> teamMeetingIds = team.getTeamMeetingIds();
        teamMeetingIds.addAll(meetingIds);
        team.setTeamMeetingIds(teamMeetingIds);
        teamRepository.save(team);
    }
}
