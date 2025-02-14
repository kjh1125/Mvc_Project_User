package kr.bit.dao;

import kr.bit.entity.Board;
import kr.bit.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BoardDao {
    @Autowired
    private BoardMapper boardMapper;

    public void insertBoard(Board board){
        System.out.println(board);
        boardMapper.insertBoard(board);
    }
}
