//package com.mingle.ZiYou.daoFactory;
//
//import com.mingle.ZiYou.daoImpl.CommentDao;
//import com.mingle.ZiYou.daoImpl.PointDao;
//import com.mingle.ZiYou.daoInterface.CommentInterface;
//import com.mingle.ZiYou.daoInterface.PointInterface;
//
///**
// * Created by maxiangyu on 2016/6/2.
// */
//public class CommentDaoFactory {
//
//    public CommentInterface getInstance(String classname)
//    {
//        CommentInterface p = (CommentDao)Class.forName(classname).newInstance();
//        return p;
//    }
//}
