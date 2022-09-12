package com.viliussutkus89.iamspeed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

// Taken from
// https://medium.com/nerd-for-tech/merging-livedata-like-you-need-it-3abcf6b756ca

// Modification:
// private val postValueInsteadOfSetValue: Boolean = false,

sealed class MergerLiveData<TargetType> : MediatorLiveData<TargetType>() {
    class Three<FirstSourceType, SecondSourceType, ThirdSourceType, TargetType>(
        private val firstSource: LiveData<FirstSourceType>,
        private val secondSource: LiveData<SecondSourceType>,
        private val thirdSource: LiveData<ThirdSourceType>,
        private val distinctUntilChanged: Boolean = true,
        private val postValueInsteadOfSetValue: Boolean = false,
        private val merging: (FirstSourceType, SecondSourceType, ThirdSourceType) -> TargetType
    ) : MediatorLiveData<TargetType>() {
        override fun onActive() {
            super.onActive()

            addSource(firstSource) { value ->
                setPostValue(
                    distinctUntilChanged = distinctUntilChanged,
                    newValue = merging(
                        value,
                        secondSource.value ?: return@addSource,
                        thirdSource.value ?: return@addSource,
                    ),
                    postValueInsteadOfSetValue = postValueInsteadOfSetValue
                )
            }

            addSource(secondSource) { value ->
                setPostValue(
                    distinctUntilChanged = distinctUntilChanged,
                    newValue = merging(
                        firstSource.value ?: return@addSource,
                        value,
                        thirdSource.value ?: return@addSource,
                    ),
                    postValueInsteadOfSetValue = postValueInsteadOfSetValue
                )
            }

            addSource(thirdSource) { value ->
                setPostValue(
                    distinctUntilChanged = distinctUntilChanged,
                    newValue = merging(
                        firstSource.value ?: return@addSource,
                        secondSource.value ?: return@addSource,
                        value
                    ),
                    postValueInsteadOfSetValue = postValueInsteadOfSetValue
                )
            }
        }

        override fun onInactive() {
            removeSource(firstSource)
            removeSource(secondSource)
            removeSource(thirdSource)

            super.onInactive()
        }
    }

    class Four<FirstSourceType, SecondSourceType, ThirdSourceType, FourthSourceType, TargetType>(
        private val firstSource: LiveData<FirstSourceType>,
        private val secondSource: LiveData<SecondSourceType>,
        private val thirdSource: LiveData<ThirdSourceType>,
        private val fourthSource: LiveData<FourthSourceType>,
        private val distinctUntilChanged: Boolean = true,
        private val postValueInsteadOfSetValue: Boolean = false,
        private val merging: (FirstSourceType, SecondSourceType, ThirdSourceType, FourthSourceType) -> TargetType
    ) : MediatorLiveData<TargetType>() {
        override fun onActive() {
            super.onActive()

            addSource(firstSource) { value ->
                setPostValue(
                    distinctUntilChanged = distinctUntilChanged,
                    newValue = merging(
                        value,
                        secondSource.value ?: return@addSource,
                        thirdSource.value ?: return@addSource,
                        fourthSource.value ?: return@addSource,
                    ),
                    postValueInsteadOfSetValue = postValueInsteadOfSetValue
                )
            }

            addSource(secondSource) { value ->
                setPostValue(
                    distinctUntilChanged = distinctUntilChanged,
                    newValue = merging(
                        firstSource.value ?: return@addSource,
                        value,
                        thirdSource.value ?: return@addSource,
                        fourthSource.value ?: return@addSource
                    ),
                    postValueInsteadOfSetValue = postValueInsteadOfSetValue
                )
            }

            addSource(thirdSource) { value ->
                setPostValue(
                    distinctUntilChanged = distinctUntilChanged,
                    newValue = merging(
                        firstSource.value ?: return@addSource,
                        secondSource.value ?: return@addSource,
                        value,
                        fourthSource.value ?: return@addSource
                    ),
                    postValueInsteadOfSetValue = postValueInsteadOfSetValue
                )
            }

            addSource(fourthSource) { value ->
                setPostValue(
                    distinctUntilChanged = distinctUntilChanged,
                    newValue = merging(
                        firstSource.value ?: return@addSource,
                        secondSource.value ?: return@addSource,
                        thirdSource.value ?: return@addSource,
                        value
                    ),
                    postValueInsteadOfSetValue = postValueInsteadOfSetValue
                )
            }
        }

        override fun onInactive() {
            removeSource(firstSource)
            removeSource(secondSource)
            removeSource(thirdSource)
            removeSource(fourthSource)

            super.onInactive()
        }
    }
}

private fun <T> MediatorLiveData<T>.postValue(
    distinctUntilChanged: Boolean,
    newValue: T
) {
    val value = value ?: postValue(newValue)

    if (distinctUntilChanged && value == newValue) return

    postValue(newValue)
}

private fun <T> MediatorLiveData<T>.setValue(
    distinctUntilChanged: Boolean,
    newValue: T
) {
    val value = value ?: setValue(newValue)

    if (distinctUntilChanged && value == newValue) return

    setValue(newValue)
}

private fun <T> MediatorLiveData<T>.setPostValue(
    distinctUntilChanged: Boolean,
    newValue: T,
    postValueInsteadOfSetValue: Boolean
) {
    if (postValueInsteadOfSetValue) {
        postValue(distinctUntilChanged, newValue)
    } else {
        setValue(distinctUntilChanged, newValue)
    }
}
