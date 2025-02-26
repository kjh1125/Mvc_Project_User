package kr.bit.dao;

import kr.bit.entity.Event;
import kr.bit.entity.Point;
import kr.bit.mapper.EventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventDao {
    @Autowired
    private EventMapper eventMapper;

    public List<Event> getCurrentEvents(){
        return eventMapper.getCurrentEvents();
    }

}
