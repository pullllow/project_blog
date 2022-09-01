package com.example.bussniess.post.reply;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.lang.Result;
import com.example.config.ApplicationBeanFactory;
import com.example.config.WebSocketConfig;
import com.example.entity.Comment;
import com.example.entity.Post;
import com.example.entity.User;
import com.example.entity.UserMessage;
import com.example.service.*;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * @author Chang Qi
 * @date 2022/9/1 10:30
 * @description
 * @Version V1.0
 */

public class ReplyThread implements Callable<Result> {

    private Long id;
    private long profileId;
    private String content;

    private PostService postService;

    private CommentService commentService;

    private UserMessageService userMessageService;

    private WebSocketService webSocketService;

    private UserService userService;

    public ReplyThread(Long id, long profileId, String content) {
        this.id = id;
        this.profileId = profileId;
        this.content = content;

        postService = ApplicationBeanFactory.getBean(PostService.class);
        commentService = ApplicationBeanFactory.getBean(CommentService.class);
        userMessageService = ApplicationBeanFactory.getBean(UserMessageService.class);
        webSocketService = ApplicationBeanFactory.getBean(WebSocketService.class);
        userService = ApplicationBeanFactory.getBean(UserService.class);

    }

    @Override
    public Result call() throws Exception {

        Post post = postService.getById(id);
        Assert.isTrue(post != null, "该文章已被删除");

        Date date = new Date();

        Comment comment = new Comment();

        comment.setPostId(id);
        comment.setContent(content);

        comment.setUserId(profileId);

        comment.setCreated(date);
        comment.setModified(date);

        comment.setLevel(0);
        comment.setVoteDown(0);
        comment.setVoteUp(0);



        commentService.save(comment);

        //博客评论数量+1
        post.setCommentCount(post.getCommentCount() + 1);
        postService.updateById(post);

        //本周热议数量+1

        postService.incrCommentCountAndUnionForWeekRank(id, true);

        //通知作者，博客被评论 (自己评论自己不通知)
        if (comment.getUserId() != post.getUserId()) {
            UserMessage message = new UserMessage();

            message.setPostId(id);
            ///
            message.setCommentId(comment.getId());

            message.setFromUserId(profileId);
            message.setToUserId(post.getUserId());

            message.setType(1);
            message.setContent(content);
            message.setCreated(date);
            message.setModified(date);

            message.setStatus(0);

            userMessageService.save(message);
            webSocketService.sendMessCountToUser(message.getToUserId());

        }

        // 通知@的人
        if (content.startsWith("@")) {
            String username = content.substring(1, content.indexOf(" "));

            User user = userService.getOne(new QueryWrapper<User>().eq("name", username));
            if (user != null) {
                UserMessage message = new UserMessage();
                message.setPostId(id);
                message.setCommentId(comment.getId());
                message.setFromUserId(profileId);
                message.setToUserId(user.getId());
                message.setType(2);
                message.setContent(content);
                message.setCreated(new Date());
                message.setStatus(0);
                userMessageService.save(message);


                // 即时通知被@的用户
                webSocketService.sendMessCountToUser(message.getToUserId());
            }

        }

        return Result.success();
    }

}
