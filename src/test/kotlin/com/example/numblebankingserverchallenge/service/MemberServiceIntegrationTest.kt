package com.example.numblebankingserverchallenge.service

import com.example.numblebankingserverchallenge.NumbleBankingServerChallengeApplication
import com.example.numblebankingserverchallenge.domain.Member
import com.example.numblebankingserverchallenge.dto.LoginRequest
import com.example.numblebankingserverchallenge.dto.SignUpRequest
import com.example.numblebankingserverchallenge.exception.CustomException
import com.example.numblebankingserverchallenge.repository.friendship.FriendshipRepository
import com.example.numblebankingserverchallenge.repository.member.MemberRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest(classes = [NumbleBankingServerChallengeApplication::class])
@ActiveProfiles("test")
@Transactional

@ExtendWith(SpringExtension::class)
class MemberServiceIntegrationTest @Autowired constructor(
    private val friendshipRepository: FriendshipRepository,
    private val passwordEncoder: PasswordEncoder,
    private val memberService: MemberService,
    private val memberRepository: MemberRepository
) {
    val username = "Inu"
    val pw1 = "12345"
    val signUpRequest = SignUpRequest(username, pw1)

    @BeforeEach
    fun init() {
        memberRepository.deleteAll()
        friendshipRepository.deleteAll()
    }

    @Test
    fun `회원가입 성공`() {
        val createdUser = memberService.createUser(signUpRequest)
        assertThat(createdUser).isNotNull
        assertThat(createdUser?.username).isEqualTo(signUpRequest.username)
        val savedUser = memberRepository.findByUsername(signUpRequest.username)
        assertThat(passwordEncoder.matches(signUpRequest.pw, savedUser?.encryptedPassword)).isTrue

    }

    @Test
    fun `이미 존재하는 유저이름으로 회원가입 실패`() {


        val createdUser = memberService.createUser(signUpRequest)
        assertThat(createdUser).isNotNull
        assertThat(createdUser?.username).isEqualTo(signUpRequest.username)
        val signUpRequest2 = SignUpRequest(username, "23456")

        assertThrows<CustomException.UserExistsException> { memberService.createUser(signUpRequest2) }

    }


    @Test
    fun `친구추가 성공`() {
        val user = Member(username, passwordEncoder.encode(pw1))
        val friend = Member("friend1", passwordEncoder.encode(pw1))
        memberRepository.saveAll(listOf(user, friend))

        val saveduser = memberRepository.findByUsername(username)
        val savedFriend = memberRepository.findByUsername("friend1")
        assertThat(saveduser).isNotNull
        assertThat(savedFriend).isNotNull
        val friendShipDTO = memberService.addFriend(saveduser!!.id, savedFriend!!.id)
        //user가 없거나 friend가 없는 경우 null 반환
        //user-> friend, friend->user, 2개 friendship

        val friends = memberService.getFriends(saveduser.id)
        val opposite = memberService.getFriends(savedFriend.id)

        assertThat(friendShipDTO).isNotNull
        assertThat(friends.size).isEqualTo(1)
        assertThat(friends[0].username).isEqualTo(saveduser.username)
        assertThat(friends[0].friendName).isEqualTo(savedFriend.username)
        assertThat(opposite.size).isEqualTo(1)
        assertThat(opposite[0].username).isEqualTo(savedFriend.username)
        assertThat(opposite[0].friendName).isEqualTo(saveduser.username)
    }

    @Test
    fun `친구추가 실패`() {
        val user = Member(username, passwordEncoder.encode(pw1))
        val friend = Member("friend1", passwordEncoder.encode(pw1))

        memberRepository.saveAll(listOf(user, friend))

        val saveduser = memberRepository.findByUsername(username)
        val savedFriend = memberRepository.findByUsername("friend2")
        assertThat(saveduser).isNotNull
        assertThat(savedFriend).isNull()
        assertThrows<CustomException.UserNotFoundException> { memberService.addFriend(saveduser!!.id, UUID.randomUUID()) }

    }

    @Test
    fun `친구목록 조회`() {
        val user = Member(username, passwordEncoder.encode(pw1))
        val friend = Member("friend1", passwordEncoder.encode(pw1))
        memberRepository.saveAll(listOf(user, friend))

        val saveduser = memberRepository.findById(user.id).orElse(null)
        val savedFriend = memberRepository.findById(friend.id).orElse(null)
        assertThat(saveduser).isNotNull
        assertThat(savedFriend).isNotNull
        val friendShipDTO = memberService.addFriend(saveduser!!.id, savedFriend!!.id)

        val friends = memberService.getFriends(saveduser.id)
        assertThat(friends).hasSize(1)
        assertThat(friends[0].friendName).isEqualTo(savedFriend.username)
        val opposite = memberService.getFriends(savedFriend.id)

        assertThat(opposite).hasSize(1)
        assertThat(opposite[0].friendName).isEqualTo(user.username)
    }

}