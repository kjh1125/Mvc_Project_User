package kr.bit.mapper;

import kr.bit.entity.Event;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface EventMapper {

    @Select("SELECT id, name, image_url AS imageUrl, start_date AS startDate, end_date AS endDate " +
            "FROM events " +
            "WHERE start_date <= CURDATE() AND end_date >= CURDATE()")
    List<Event> getCurrentEvents();

}
