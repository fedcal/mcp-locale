"""Repository layer per eventi con supporto opzionale MySQL via SQLAlchemy."""

from __future__ import annotations

from dataclasses import asdict
from typing import Iterable

from sqlalchemy import Column, Float, MetaData, String, Text, create_engine
from sqlalchemy.dialects.mysql import DECIMAL
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, relationship

from .models import Event, Participant, new_event, new_participant


class Base(DeclarativeBase):
    metadata = MetaData()


class EventModel(Base):
    __tablename__ = "events"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    name: Mapped[str] = mapped_column(String(255))
    date: Mapped[str] = mapped_column(String(255))
    location: Mapped[str] = mapped_column(String(255))
    budget: Mapped[float | None] = mapped_column(DECIMAL(12, 2), nullable=True)
    currency: Mapped[str] = mapped_column(String(16))
    notes: Mapped[str | None] = mapped_column(Text(), nullable=True)
    participants: Mapped[list["ParticipantModel"]] = relationship(back_populates="event", cascade="all, delete-orphan")


class ParticipantModel(Base):
    __tablename__ = "participants"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    name: Mapped[str] = mapped_column(String(255))
    intolerances: Mapped[str | None] = mapped_column(Text(), nullable=True)
    preferences: Mapped[str | None] = mapped_column(Text(), nullable=True)
    weight: Mapped[float] = mapped_column(Float(), default=1.0)
    event_id: Mapped[str] = mapped_column(String(32))
    event: Mapped[EventModel] = relationship(back_populates="participants")


class EventRepository:
    """Persistenza basata su SQLAlchemy (MySQL compatibile) con fallback in-memory."""

    def __init__(self, db_url: str | None = None):
        self.db_url = db_url
        self._memory_events: dict[str, Event] = {}
        if db_url:
            self.engine = create_engine(db_url)
            Base.metadata.create_all(self.engine)
        else:
            self.engine = None

    # -- Eventi --
    def save_event(self, event: Event) -> Event:
        if not self.engine:
            self._memory_events[event.id] = event
            return event
        with Session(self.engine) as session:
            model = EventModel(
                id=event.id,
                name=event.name,
                date=event.date,
                location=event.location,
                budget=event.budget,
                currency=event.currency,
                notes=event.notes,
            )
            session.add(model)
            session.commit()
        return event

    def get_event(self, event_id: str) -> Event | None:
        if not self.engine:
            return self._memory_events.get(event_id)
        with Session(self.engine) as session:
            model = session.get(EventModel, event_id)
            if not model:
                return None
            participants = [
                new_participant(
                    name=p.name,
                    intolerances=self._split_csv(p.intolerances),
                    preferences=self._split_csv(p.preferences),
                    weight=p.weight,
                )
                for p in model.participants
            ]
            ev = new_event(model.name, model.date, model.location, model.currency, model.budget, model.notes)
            ev.id = model.id  # preserve
            ev.participants = participants
            return ev

    # -- Partecipanti --
    def add_participant(self, event_id: str, participant: Participant) -> Participant:
        if not self.engine:
            event = self._memory_events[event_id]
            event.participants.append(participant)
            return participant
        with Session(self.engine) as session:
            model = ParticipantModel(
                id=participant.id,
                name=participant.name,
                intolerances=",".join(participant.intolerances),
                preferences=",".join(participant.preferences),
                weight=participant.weight,
                event_id=event_id,
            )
            session.add(model)
            session.commit()
        return participant

    def update_participant(
        self,
        event_id: str,
        participant_id: str,
        intolerances: Iterable[str] | None,
        preferences: Iterable[str] | None,
        weight: float | None,
    ) -> Participant | None:
        if not self.engine:
            event = self._memory_events.get(event_id)
            if not event:
                return None
            target = next((p for p in event.participants if p.id == participant_id), None)
            if not target:
                return None
            if intolerances is not None:
                target.intolerances = list(intolerances)
            if preferences is not None:
                target.preferences = list(preferences)
            if weight is not None and weight > 0:
                target.weight = weight
            return target

        with Session(self.engine) as session:
            model = session.get(ParticipantModel, participant_id)
            if not model:
                return None
            if intolerances is not None:
                model.intolerances = ",".join(intolerances)
            if preferences is not None:
                model.preferences = ",".join(preferences)
            if weight is not None and weight > 0:
                model.weight = weight
            session.commit()
            return self._to_participant(model)

    def list_participants(self, event_id: str) -> list[Participant]:
        if not self.engine:
            ev = self._memory_events.get(event_id)
            return list(ev.participants) if ev else []
        with Session(self.engine) as session:
            models = session.query(ParticipantModel).filter(ParticipantModel.event_id == event_id).all()
            return [self._to_participant(m) for m in models]

    def _to_participant(self, model: ParticipantModel) -> Participant:
        return Participant(
            id=model.id,
            name=model.name,
            intolerances=self._split_csv(model.intolerances),
            preferences=self._split_csv(model.preferences),
            weight=model.weight,
        )

    def _split_csv(self, text: str | None) -> list[str]:
        if not text:
            return []
        return [item.strip() for item in text.split(",") if item.strip()]
