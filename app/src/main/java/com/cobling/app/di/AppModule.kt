package com.cobling.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// LocalStorageManager, ReviewManager, AuthViewModel 등은 모두
// @Singleton + @Inject constructor() 를 가지므로 Hilt가 자동 제공합니다.
// 추가 바인딩이 필요할 때 이 파일에 @Provides 함수를 작성하세요.
@Module
@InstallIn(SingletonComponent::class)
object AppModule
