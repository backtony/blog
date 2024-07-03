package com.sample.hexagonal.sample.infrastructure.h2.sample

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SampleEntityDao : JpaRepository<SampleEntity, Long>
