package kr.bit.dao;

import kr.bit.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BoardDao {
    @Autowired
    private BoardMapper boardMapper;

}
