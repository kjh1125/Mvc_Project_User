package kr.bit.service;

import kr.bit.dao.EventDao;
import kr.bit.dao.UserDao;
import kr.bit.entity.Event;
import kr.bit.entity.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventDao eventDao;

    public List<Event> getCurrentEvents(){
        return eventDao.getCurrentEvents();
    }

}
