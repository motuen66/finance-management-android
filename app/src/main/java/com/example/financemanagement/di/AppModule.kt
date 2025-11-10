@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideApiService(): ApiService = RetrofitClient.apiService

    @Provides
    fun provideAuthRepository(api: ApiService) = AuthRepository(api)
}
