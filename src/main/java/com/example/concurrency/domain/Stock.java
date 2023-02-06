package com.example.concurrency.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "stock")
public class Stock {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long productId;

	private Long quantity;

	@Builder
	Stock(Long id, Long productId, Long quantity){
		this.id = id;
		this.productId = productId;
		this.quantity = quantity;
	}

	public void decrease(Long quantity){
		if (this.quantity < quantity){
			throw new IllegalArgumentException("");
		}
		this.quantity -= quantity;
	}
}
