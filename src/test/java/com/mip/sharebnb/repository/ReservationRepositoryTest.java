package com.mip.sharebnb.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mip.sharebnb.model.Accommodation;
import com.mip.sharebnb.model.BookedDate;
import com.mip.sharebnb.model.Member;
import com.mip.sharebnb.model.Reservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.config.location="
        + "classpath:application.yml,"
        + "classpath:datasource.yml")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private MemberRepository memberRepository;
    private LocalDate bookedDate;

    @DisplayName("예약 내역 조회 리스트")
    @Test
    @Transactional
    public void getReservationByMemberId() throws JsonProcessingException {

        List<Reservation> reservationByMember = reservationRepository.findReservationByMemberId(1L);
        System.out.println(">>>>>>" + reservationByMember);

        Reservation result = reservationByMember.get(0);
        System.out.println(result);

        assertThat(reservationByMember.size()).isEqualTo(2);
        assertThat(reservationByMember.get(0).getTotalPrice()).isEqualTo(20000);
        assertThat(reservationByMember.get(0).getAccommodation().getAccommodationType()).isEqualTo("집전체");
        assertThat(reservationByMember.get(0).getAccommodation().getBuildingType()).isEqualTo("단독주택");

    }

    @DisplayName("중복된 체크인날짜인지 확인")
    @Test
    @Transactional
    public void checkDuplicateCheckInDate(){
        Optional<Reservation> findReservation = reservationRepository.findById(1L);

        Reservation reservation = findReservation.get();
        Long id = reservation.getAccommodation().getId();

        Optional<Accommodation> findAccommodation = accommodationRepository.findById(id);
        List<BookedDate> bookedDates = findAccommodation.get().getBookedDates();

        LocalDate duplicateBookedDate = bookedDates.get(0).getDate();
        LocalDate differentBookedDate = bookedDates.get(1).getDate();

        assertThat(reservation.getCheckInDate().equals(duplicateBookedDate)).isTrue();
        assertThat(reservation.getCheckInDate().equals(differentBookedDate)).isFalse();

    }

    private void setReservations(){
        List<Reservation> reservations = new ArrayList<>();

        Member member = new Member();
        LocalDate birtDate = LocalDate.of(2020,1,14);
        member.setEmail("test123@gmail.com");
        member.setPassword("1234");
        member.setBirthDate(birtDate);
        member.setContact("01022223333");

        Member saveMember = memberRepository.save(member);

        Accommodation accommodation = new Accommodation();
        accommodation.setBathroomNum(2);
        accommodation.setBedroomNum(2);
        accommodation.setAccommodationType("집전체");
        accommodation.setBuildingType("아파트");
        Accommodation findAccommodation1 = accommodationRepository.save(accommodation);

        Reservation reservation1 = new Reservation();
        LocalDate checkIn = LocalDate.of(2020,1,12);
        LocalDate checkout = LocalDate.of(2020,1,14);
        reservation1.setCheckInDate(checkIn);
        reservation1.setCheckoutDate(checkout);
        reservation1.setGuestNum(3);
        reservation1.setTotalPrice(20000);
        reservation1.setPaymentDate(LocalDate.now());
        reservation1.setAccommodation(findAccommodation1);
        reservation1.setMember(saveMember);

        reservationRepository.save(reservation1);

        reservations.add(reservation1);

        Accommodation accommodation2 = new Accommodation();
        accommodation2.setBathroomNum(2);
        accommodation2.setBedroomNum(5);
        accommodation2.setAccommodationType("집전체");
        accommodation2.setBuildingType("단독주택");

        Accommodation findAccommodation2 = accommodationRepository.save(accommodation2);

        Reservation reservation2 = new Reservation();
        LocalDate checkIn2 = LocalDate.of(2020,1,14);
        LocalDate checkout2 = LocalDate.of(2020,1,16);
        reservation2.setCheckInDate(checkIn2);
        reservation2.setCheckoutDate(checkout2);
        reservation2.setGuestNum(4);
        reservation2.setTotalPrice(10000);
        reservation2.setPaymentDate(LocalDate.now());
        reservation2.setAccommodation(findAccommodation2);
        reservation2.setMember(saveMember);

        reservationRepository.save(reservation2);
        reservations.add(reservation2);

    }
}