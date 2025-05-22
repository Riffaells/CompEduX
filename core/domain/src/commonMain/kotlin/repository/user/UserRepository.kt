package repository.user

import model.DomainResult
import model.users.UsersListDomain


interface UserRepository {


    fun getUsers(): DomainResult<UsersListDomain>

    fun getFriends(): String?


    fun addFriends(): String?
} 