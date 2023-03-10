package com.example.numblebankingserverchallenge.controller

import com.example.numblebankingserverchallenge.config.SessionLoginChecker
import com.example.numblebankingserverchallenge.dto.FriendDTO
import com.example.numblebankingserverchallenge.dto.FriendRequest
import com.example.numblebankingserverchallenge.dto.MemberDTO
import com.example.numblebankingserverchallenge.dto.SignUpRequest
import com.example.numblebankingserverchallenge.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class MemberController (private val memberService: MemberService){
    @PostMapping("/signup")
    fun signup(@RequestBody signUpRequest: SignUpRequest):ResponseEntity<MemberDTO>{
        return memberService.createUser(signUpRequest).let{ ResponseEntity.ok().body(it)}
    }

    @GetMapping("/users/friends")
    fun friendsList(@SessionLoginChecker member:MemberDTO):ResponseEntity<List<FriendDTO>>{
        return memberService.getFriends(member.id).let { ResponseEntity.ok().body(it) }
    }

    @PostMapping("/users/friends")
    fun addFriend(@RequestBody friendRequest: FriendRequest, @SessionLoginChecker member: MemberDTO):ResponseEntity<FriendDTO>{
        return memberService.addFriend(member.id,friendRequest.friendId).let { ResponseEntity.ok(it) }
    }
}