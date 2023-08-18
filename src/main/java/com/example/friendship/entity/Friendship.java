package com.example.friendship.entity;

import com.example.friendship.states.ChatState;
import com.example.friendship.states.FriendshipState;
import com.example.user.entity.User;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "friendship")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    @ToString.Exclude
    private User follower;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    @ToString.Exclude
    private User friend;

    @Enumerated(EnumType.STRING)
    private FriendshipState state;
    @Enumerated(EnumType.STRING)
    private ChatState chat;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(id, that.id)
                && Objects.equals(follower, that.follower)
                && Objects.equals(friend, that.friend)
                && state == that.state && chat == that.chat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, follower, friend, state, chat);
    }
}
